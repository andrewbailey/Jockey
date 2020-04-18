package dev.andrewbailey.encore.player.playback

import android.content.Context
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.audio.AudioAttributes
import dev.andrewbailey.encore.player.BuildConfig
import dev.andrewbailey.encore.player.state.MediaPlayerState
import dev.andrewbailey.encore.player.state.RepeatMode
import dev.andrewbailey.encore.player.state.TransportState
import dev.andrewbailey.encore.player.state.diff.*
import dev.andrewbailey.encore.player.state.factory.PlaybackStateFactory

internal class MediaPlayer(
    context: Context,
    userAgent: String = "Encore/${BuildConfig.VERSION_NAME}",
    playbackStateFactory: PlaybackStateFactory,
    private val extensions: List<PlaybackExtension>,
    private val observers: List<PlaybackObserver>
) {

    private val exoPlayer = ExoPlayerFactory.newSimpleInstance(context)
    private val queue = MediaQueue(context, userAgent)

    private val stateLock = Any()
    private val differ = PlaybackStateDiffer()
    private val stateCreator = PlaybackStateCreator(exoPlayer, queue)
    private var lastDispatchedState: MediaPlayerState? = null
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

    fun getState(): MediaPlayerState {
        return synchronized(stateLock) {
            stateCreator.createPlaybackState()
        }
    }

    private fun getTransportState(): TransportState {
        return synchronized(stateLock) {
            stateCreator.createTransportState()
        }
    }

    fun setState(state: TransportState) {
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
                        queue.changeQueue(operation.newQueue)
                        exoPlayer.prepare(queue.mediaSource)
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
                extensions.forEach { it.onNewPlayerState(state) }
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
