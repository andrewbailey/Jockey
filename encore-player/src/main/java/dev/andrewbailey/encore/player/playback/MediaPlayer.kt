package dev.andrewbailey.encore.player.playback

import android.content.Context
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.audio.AudioAttributes
import dev.andrewbailey.encore.model.MediaObject
import dev.andrewbailey.encore.player.BuildConfig
import dev.andrewbailey.encore.player.state.MediaPlayerState
import dev.andrewbailey.encore.player.state.RepeatMode
import dev.andrewbailey.encore.player.state.TransportState
import dev.andrewbailey.encore.player.state.diff.PlaybackStateDiffer
import dev.andrewbailey.encore.player.state.diff.QueueModification
import dev.andrewbailey.encore.player.state.diff.SetPlaying
import dev.andrewbailey.encore.player.state.diff.SetRepeatMode
import dev.andrewbailey.encore.player.state.diff.StopPlayback
import dev.andrewbailey.encore.player.state.diff.TimelinePositionChange
import dev.andrewbailey.encore.player.state.factory.PlaybackStateFactory

internal class MediaPlayer<M : MediaObject>(
    context: Context,
    userAgent: String = "Encore/${BuildConfig.VERSION_NAME}",
    playbackStateFactory: PlaybackStateFactory<M>,
    private val extensions: List<PlaybackExtension<M>>,
    private val observers: List<PlaybackObserver<M>>
) {

    private val exoPlayer = SimpleExoPlayer.Builder(context).build()
    private val queue = MediaQueue<M>(context, userAgent)

    private val stateLock = Any()
    private val differ = PlaybackStateDiffer<M>()
    private val stateCreator = PlaybackStateCreator<M>(exoPlayer, queue)
    private var lastDispatchedState: MediaPlayerState<M>? = null
    private var shouldDispatchStateChanges: Boolean = true

    init {
        extensions.forEach { it.initialize(this, playbackStateFactory) }

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
    }

    fun getState(): MediaPlayerState<M> {
        return synchronized(stateLock) {
            stateCreator.createPlaybackState()
        }
    }

    private fun getTransportState(): TransportState<M> {
        return synchronized(stateLock) {
            stateCreator.createTransportState()
        }
    }

    fun setState(state: TransportState<M>) {
        synchronized(stateLock) {
            shouldDispatchStateChanges = false

            val diff = differ.generateDiff(getTransportState(), state)

            for (operation in diff.operations) {
                when (operation) {
                    is TimelinePositionChange -> {
                        exoPlayer.seekTo(operation.queueIndex, operation.seekPositionMillis)
                    }
                    is SetPlaying -> {
                        exoPlayer.playWhenReady = operation.isPlaying
                    }
                    is StopPlayback -> {
                        exoPlayer.stop(true)
                    }
                    is SetRepeatMode -> {
                        exoPlayer.repeatMode = when (operation.repeatMode) {
                            RepeatMode.REPEAT_NONE -> Player.REPEAT_MODE_OFF
                            RepeatMode.REPEAT_ALL -> Player.REPEAT_MODE_ALL
                            RepeatMode.REPEAT_ONE -> Player.REPEAT_MODE_ONE
                        }
                    }
                    is QueueModification -> {
                        queue.updateQueue(operation.updatedQueue).also {
                            if (!operation.isSeamless) {
                                exoPlayer.prepare(queue.mediaSource)
                            }
                        }
                    }
                }.let { /* Require when to be exhaustive */ }
            }

            shouldDispatchStateChanges = true
            dispatchStateChange()
        }
    }

    private fun dispatchStateChange() {
        if (shouldDispatchStateChanges) {
            val state = getState()
            if (state != lastDispatchedState) {
                lastDispatchedState = state
                extensions.forEach { it.dispatchNewState(state) }
                observers.forEach { it.onPlaybackStateChanged(state) }
            }
        }
    }

    fun release() {
        extensions.forEach { it.release() }
        observers.forEach { it.onRelease() }
        exoPlayer.release()
    }

}
