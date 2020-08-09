package dev.andrewbailey.encore.player.playback

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player.*
import com.google.android.exoplayer2.metadata.flac.PictureFrame
import com.google.android.exoplayer2.metadata.id3.ApicFrame
import dev.andrewbailey.encore.player.playback.MediaQueueItems.*
import dev.andrewbailey.encore.player.state.BufferingState
import dev.andrewbailey.encore.player.state.MediaPlayerState
import dev.andrewbailey.encore.player.state.PlaybackState.*
import dev.andrewbailey.encore.player.state.QueueState.*
import dev.andrewbailey.encore.player.state.RepeatMode.*
import dev.andrewbailey.encore.player.state.SeekPosition.AbsoluteSeekPosition
import dev.andrewbailey.encore.player.state.SeekPosition.ComputedSeekPosition
import dev.andrewbailey.encore.player.state.ShuffleMode.LINEAR
import dev.andrewbailey.encore.player.state.TransportState
import dev.andrewbailey.encore.player.util.getEntries
import dev.andrewbailey.encore.player.util.getFormats
import dev.andrewbailey.encore.player.util.toList

internal class PlaybackStateCreator(
    private val exoPlayer: ExoPlayer,
    private val queue: MediaQueue
) {

    fun createPlaybackState(): MediaPlayerState {
        return when (val transportState = createTransportState()) {
            is TransportState.Active -> MediaPlayerState.Prepared(
                transportState = transportState,
                artwork = getArtwork(),
                durationMs = exoPlayer.duration.takeIf { it != C.TIME_UNSET },
                bufferingState = getBufferingState()
            )
            is TransportState.Idle -> MediaPlayerState.Ready(
                transportState = transportState
            )
        }
    }

    fun createTransportState(): TransportState {
        val queueItems = queue.queueItems
        return if (queueItems == null) {
            TransportState.Idle(
                repeatMode = getRepeatMode(),
                shuffleMode = LINEAR
            )
        } else {
            TransportState.Active(
                status = when {
                    exoPlayer.playbackState == STATE_ENDED -> REACHED_END
                    exoPlayer.playWhenReady -> PLAYING
                    else -> PAUSED
                },
                seekPosition = when {
                    exoPlayer.playWhenReady && exoPlayer.playbackState == STATE_READY -> {
                        ComputedSeekPosition(
                            originalSeekPositionMillis = exoPlayer.currentPosition,
                            maxSeekPositionMillis = exoPlayer.contentDuration
                                .takeIf { it != C.TIME_UNSET }
                                ?: Long.MAX_VALUE
                        )
                    }
                    else -> {
                        AbsoluteSeekPosition(
                            seekPositionMillis = exoPlayer.currentPosition
                        )
                    }
                },
                queue = when (queueItems) {
                    is LinearQueueItems -> Linear(
                        queue = queueItems.queue,
                        queueIndex = exoPlayer.currentWindowIndex
                    )
                    is ShuffledQueueItems -> Shuffled(
                        queue = queueItems.queue,
                        queueIndex = exoPlayer.currentWindowIndex,
                        linearQueue = queueItems.linearQueue
                    )
                },
                repeatMode = getRepeatMode()
            )
        }
    }

    private fun getArtwork(): Bitmap? {
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

    private fun getBufferingState(): BufferingState {
        return when {
            exoPlayer.playbackState == STATE_BUFFERING -> BufferingState.Buffering(
                pausedForBuffering = true,
                bufferedAmountMs = exoPlayer.bufferedPosition.toInt()
            )
            exoPlayer.bufferedPercentage < 100 -> BufferingState.Buffering(
                pausedForBuffering = false,
                bufferedAmountMs = exoPlayer.bufferedPosition.toInt()
            )
            else -> BufferingState.Buffered
        }
    }

    private fun getRepeatMode() = when (val repeatMode = exoPlayer.repeatMode) {
        REPEAT_MODE_OFF -> REPEAT_NONE
        REPEAT_MODE_ONE -> REPEAT_ONE
        REPEAT_MODE_ALL -> REPEAT_ALL
        else -> throw IllegalStateException("Invalid exoPlayer repeat mode $repeatMode")
    }

}
