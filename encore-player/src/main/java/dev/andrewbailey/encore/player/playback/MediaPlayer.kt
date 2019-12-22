package dev.andrewbailey.encore.player.playback

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.metadata.flac.PictureFrame
import com.google.android.exoplayer2.metadata.id3.ApicFrame
import dev.andrewbailey.encore.player.BuildConfig
import dev.andrewbailey.encore.player.state.*
import dev.andrewbailey.encore.player.state.SeekPosition.AbsoluteSeekPosition
import dev.andrewbailey.encore.player.state.SeekPosition.ComputedSeekPosition
import dev.andrewbailey.encore.player.state.diff.*
import dev.andrewbailey.encore.player.util.getEntries
import dev.andrewbailey.encore.player.util.getFormats
import dev.andrewbailey.encore.player.util.toList

internal class MediaPlayer(
    context: Context,
    userAgent: String = "Encore/${BuildConfig.VERSION_NAME}",
    private val extensions: List<PlaybackExtension>,
    private val observers: List<PlaybackObserver>
) {

    private val exoPlayer = ExoPlayerFactory.newSimpleInstance(context)
    private val queue = MediaQueue(context, userAgent)

    private val stateLock = Any()
    private val differ = PlaybackStateDiffer()
    private var lastDispatchedState: PlaybackState? = null
    private var shouldDispatchStateChanges: Boolean = true

    init {
        extensions.forEach { it.initialize(this) }

        val exoPlayerListeners = ExoPlayerListeners(this::dispatchStateChange)
        exoPlayer.addListener(exoPlayerListeners)
    }

    fun getState(): PlaybackState {
        return synchronized(stateLock) {
            val queue = queue.queueItems

            PlaybackState(
                transportState = if (queue.isEmpty()) {
                    Idle
                } else {
                    Active(
                        status = when {
                            exoPlayer.isPlaying -> Status.PLAYING
                            exoPlayer.playbackState == Player.STATE_ENDED -> Status.REACHED_END
                            else -> Status.PAUSED
                        },
                        seekPosition = if (exoPlayer.isPlaying) {
                            ComputedSeekPosition(exoPlayer.currentPosition)
                        } else {
                            AbsoluteSeekPosition(exoPlayer.currentPosition)
                        },
                        queue = queue,
                        queueIndex = exoPlayer.currentWindowIndex
                    )
                },
                repeatMode = when (val repeatMode = exoPlayer.repeatMode) {
                    Player.REPEAT_MODE_OFF -> RepeatMode.REPEAT_NONE
                    Player.REPEAT_MODE_ONE -> RepeatMode.REPEAT_ONE
                    Player.REPEAT_MODE_ALL -> RepeatMode.REPEAT_ALL
                    else -> {
                        throw IllegalStateException("Unknown ExoPlayer repeat mode: $repeatMode")
                    }
                },
                shuffleMode = if (exoPlayer.shuffleModeEnabled) {
                    ShuffleMode.LINEAR
                } else {
                    ShuffleMode.SHUFFLED
                }
            )
        }
    }

    fun setState(state: PlaybackState) {
        synchronized(stateLock) {
            shouldDispatchStateChanges = false

            val diff = differ.generateDiff(getState(), state)

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

    fun getArtwork(): Bitmap? {
        return exoPlayer.currentTrackSelections.toList()
            .flatMap { it.getFormats() }
            .mapNotNull { it.metadata }
            .flatMap { it.getEntries() }
            .asSequence()
            .mapNotNull { metadataEntry ->
                when (metadataEntry) {
                    is ApicFrame -> {
                        metadataEntry.pictureData
                    }
                    is PictureFrame -> {
                        metadataEntry.pictureData
                    }
                    else -> null
                }
            }
            .firstOrNull()
            ?.let { BitmapFactory.decodeByteArray(it, 0, it.size) }
    }

    fun release() {
        extensions.forEach { it.release() }
        observers.forEach { it.onRelease() }
        exoPlayer.release()
    }

}
