package dev.andrewbailey.encore.player.playback

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player.REPEAT_MODE_ALL
import com.google.android.exoplayer2.Player.REPEAT_MODE_OFF
import com.google.android.exoplayer2.Player.REPEAT_MODE_ONE
import com.google.android.exoplayer2.Player.STATE_BUFFERING
import com.google.android.exoplayer2.Player.STATE_ENDED
import com.google.android.exoplayer2.Player.STATE_READY
import com.google.android.exoplayer2.Timeline
import dev.andrewbailey.encore.model.MediaObject
import dev.andrewbailey.encore.player.playback.MediaQueueItems.LinearQueueItems
import dev.andrewbailey.encore.player.playback.MediaQueueItems.ShuffledQueueItems
import dev.andrewbailey.encore.player.state.BufferingState
import dev.andrewbailey.encore.player.state.MediaPlayerState
import dev.andrewbailey.encore.player.state.PlaybackStatus.Paused
import dev.andrewbailey.encore.player.state.PlaybackStatus.Playing
import dev.andrewbailey.encore.player.state.PlaybackStatus.ReachedEnd
import dev.andrewbailey.encore.player.state.QueueState.Linear
import dev.andrewbailey.encore.player.state.QueueState.Shuffled
import dev.andrewbailey.encore.player.state.RepeatMode.REPEAT_ALL
import dev.andrewbailey.encore.player.state.RepeatMode.REPEAT_NONE
import dev.andrewbailey.encore.player.state.RepeatMode.REPEAT_ONE
import dev.andrewbailey.encore.player.state.SeekPosition.AbsoluteSeekPosition
import dev.andrewbailey.encore.player.state.SeekPosition.ComputedSeekPosition
import dev.andrewbailey.encore.player.state.ShuffleMode.LINEAR
import dev.andrewbailey.encore.player.state.TransportState

internal class PlaybackStateCreator<M : MediaObject>(
    private val exoPlayer: ExoPlayer,
    private val queue: MediaQueue<M>
) {

    fun createPlaybackState(): MediaPlayerState<M> {
        return when (val transportState = createTransportState()) {
            is TransportState.Active<M> -> MediaPlayerState.Prepared(
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

    fun createTransportState(): TransportState<M> {
        val queueItems = queue.queueItems
        return if (queueItems == null) {
            TransportState.Idle(
                repeatMode = getRepeatMode(),
                shuffleMode = LINEAR
            )
        } else {
            TransportState.Active(
                status = when {
                    exoPlayer.playbackState == STATE_ENDED ||
                        exoPlayer.isAtEndOfTimeline() -> ReachedEnd
                    exoPlayer.playWhenReady -> Playing
                    else -> Paused
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
                        queueIndex = exoPlayer.currentMediaItemIndex
                    )
                    is ShuffledQueueItems -> Shuffled(
                        queue = queueItems.queue,
                        queueIndex = exoPlayer.currentMediaItemIndex,
                        linearQueue = queueItems.linearQueue
                    )
                },
                repeatMode = getRepeatMode()
            )
        }
    }

    private fun getArtwork(): Bitmap? {
        return exoPlayer.mediaMetadata.artworkData
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

    private fun ExoPlayer.isAtEndOfTimeline(): Boolean {
        val timeline = currentTimeline
        if (isCurrentMediaItemLive || timeline.isEmpty) {
            return false
        }

        val window = timeline.getWindow(currentMediaItemIndex, Timeline.Window())
        val period = timeline.getPeriod(currentPeriodIndex, Timeline.Period())
        val isLastTrack = timeline.isLastPeriod(
            currentPeriodIndex,
            period,
            window,
            repeatMode,
            shuffleModeEnabled
        )

        val trackDuration = period.durationMs.takeIf { it != C.TIME_UNSET } ?: return false
        val currentPosition = currentPosition

        return (isLastTrack && trackDuration - currentPosition <= TRACK_END_THRESHOLD_MS)
    }

    companion object {
        private const val TRACK_END_THRESHOLD_MS = 5
    }

}
