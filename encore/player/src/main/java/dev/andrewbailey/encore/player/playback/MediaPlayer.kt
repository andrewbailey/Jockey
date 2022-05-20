package dev.andrewbailey.encore.player.playback

import android.content.Context
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.PlaybackParameters
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.audio.AudioAttributes
import dev.andrewbailey.encore.model.MediaObject
import dev.andrewbailey.encore.player.BuildConfig
import dev.andrewbailey.encore.player.state.MediaPlaybackState
import dev.andrewbailey.encore.player.state.MediaPlayerState
import dev.andrewbailey.encore.player.state.RepeatMode
import dev.andrewbailey.encore.player.state.diff.PlaybackStateDiffer
import dev.andrewbailey.encore.player.state.diff.QueueModification
import dev.andrewbailey.encore.player.state.diff.SetPlaybackSpeed
import dev.andrewbailey.encore.player.state.diff.SetPlaying
import dev.andrewbailey.encore.player.state.diff.SetRepeatMode
import dev.andrewbailey.encore.player.state.diff.StopPlayback
import dev.andrewbailey.encore.player.state.diff.TimelinePositionChange
import dev.andrewbailey.encore.player.state.factory.PlaybackStateFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

internal class MediaPlayer<M : MediaObject>(
    context: Context,
    userAgent: String = "Encore/${BuildConfig.VERSION_NAME}",
    playbackStateFactory: PlaybackStateFactory<M>,
    private val extensions: List<PlaybackExtension<M>>,
    private val observers: List<PlaybackObserver<M>>
) {

    private val exoPlayer = ExoPlayer.Builder(context).build()
    private val queue = MediaQueue<M>(context, userAgent)

    private val coroutineScope = CoroutineScope(Dispatchers.Main.immediate)
    private var isInitializationComplete = false

    private val stateLock = Any()
    private val differ = PlaybackStateDiffer<M>()
    private val stateCreator = PlaybackStateCreator<M>(exoPlayer, queue)
    private var lastDispatchedState: MediaPlayerState<M>? = null
    private var shouldDispatchStateChanges: Boolean = true

    init {
        extensions.forEach { it.attachToPlayer(this, playbackStateFactory) }

        exoPlayer.apply {
            addListener(ExoPlayerListeners(this@MediaPlayer::dispatchStateChange))
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.CONTENT_TYPE_MUSIC)
                    .build(),
                true
            )
        }

        coroutineScope.launch {
            val initialState = extensions.fold(null as MediaPlaybackState<M>?) { acc, extension ->
                extension.interceptInitialPlayerState(acc)
            }

            synchronized(stateLock) {
                isInitializationComplete = true
                initialState?.let { setState(it) }
            }

            extensions.forEach { it.dispatchPlayerInitialized() }
        }
    }

    fun getState(): MediaPlayerState<M> {
        return synchronized(stateLock) {
            if (isInitializationComplete) {
                stateCreator.createPlaybackState()
            } else {
                MediaPlayerState.Initializing
            }
        }
    }

    private fun getTransportState(): MediaPlaybackState<M> {
        return synchronized(stateLock) {
            stateCreator.createTransportState()
        }
    }

    fun setState(state: MediaPlaybackState<M>) {
        synchronized(stateLock) {
            check(isInitializationComplete) {
                "Cannot change the player's state because it is still initializing."
            }

            shouldDispatchStateChanges = false

            val diff = differ.generateDiff(getTransportState(), state)

            for (operation in diff.operations) {
                when (operation) {
                    is TimelinePositionChange -> {
                        if (operation.seekPositionMillis != Long.MAX_VALUE) {
                            exoPlayer.seekTo(operation.queueIndex, operation.seekPositionMillis)
                        } else {
                            // Seek to the end of the track by seeking to approximately infinity.
                            // Under the hood, ExoPlayer converts this seek position from
                            // milliseconds to microseconds. If we pass in MAX_VALUE directly, we'll
                            // get an integer overflow and go into the negatives, which gets treated
                            // as zero. Bit shifting by 10 bits (i.e. dividing by 1024), we give
                            // ExoPlayer enough padding to make this conversion.
                            exoPlayer.seekTo(operation.queueIndex, Long.MAX_VALUE.ushr(10))
                        }
                    }
                    is SetPlaying -> {
                        exoPlayer.playWhenReady = operation.isPlaying
                    }
                    is StopPlayback -> {
                        exoPlayer.stop()
                        exoPlayer.clearMediaItems()
                    }
                    is SetRepeatMode -> {
                        exoPlayer.repeatMode = when (operation.repeatMode) {
                            RepeatMode.RepeatNone -> Player.REPEAT_MODE_OFF
                            RepeatMode.RepeatAll -> Player.REPEAT_MODE_ALL
                            RepeatMode.RepeatOne -> Player.REPEAT_MODE_ONE
                        }
                    }
                    is QueueModification -> {
                        queue.updateQueue(operation.updatedQueue).also {
                            if (!operation.isSeamless) {
                                exoPlayer.clearMediaItems()
                                exoPlayer.setMediaSource(queue.mediaSource)
                                exoPlayer.prepare()
                            }
                        }
                    }
                    is SetPlaybackSpeed -> {
                        exoPlayer.playbackParameters = PlaybackParameters(
                            operation.speed,
                            operation.pitch
                        )
                    }
                }.let { /* Require when to be exhaustive */ }
            }

            shouldDispatchStateChanges = true
            dispatchStateChange()
        }
    }

    private fun dispatchStateChange() {
        if (isInitializationComplete && shouldDispatchStateChanges) {
            val state = getState()
            if (state != lastDispatchedState) {
                lastDispatchedState = state
                if (state is MediaPlayerState.Initialized) {
                    extensions.forEach { it.dispatchNewState(state) }
                    observers.forEach { it.onPlaybackStateChanged(state) }
                }
            }
        }
    }

    fun release() {
        extensions.forEach { it.release() }
        observers.forEach { it.onRelease() }
        exoPlayer.release()
    }

}
