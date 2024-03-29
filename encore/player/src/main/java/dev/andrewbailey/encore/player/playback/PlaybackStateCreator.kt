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
import dev.andrewbailey.encore.player.state.MediaPlaybackState
import dev.andrewbailey.encore.player.state.MediaPlayerState
import dev.andrewbailey.encore.player.state.PlaybackStatus.Paused
import dev.andrewbailey.encore.player.state.PlaybackStatus.Playing
import dev.andrewbailey.encore.player.state.QueueState.Linear
import dev.andrewbailey.encore.player.state.QueueState.Shuffled
import dev.andrewbailey.encore.player.state.RepeatMode.RepeatAll
import dev.andrewbailey.encore.player.state.RepeatMode.RepeatNone
import dev.andrewbailey.encore.player.state.RepeatMode.RepeatOne
import dev.andrewbailey.encore.player.state.SeekPosition.AbsoluteSeekPosition
import dev.andrewbailey.encore.player.state.SeekPosition.ComputedSeekPosition
import dev.andrewbailey.encore.player.state.ShuffleMode.ShuffleDisabled
import java.util.UUID

internal class PlaybackStateCreator<M : MediaObject>(
    private val exoPlayer: ExoPlayer,
    private val queue: MediaQueue<M>
) {

    fun createPlaybackState(): MediaPlayerState<M> {
        return when (val mediaPlaybackState = createMediaPlaybackState()) {
            is MediaPlaybackState.Populated<M> -> MediaPlayerState.Prepared(
                mediaPlaybackState = mediaPlaybackState,
                artwork = getArtwork(),
                durationMs = exoPlayer.duration.takeIf { it != C.TIME_UNSET },
                bufferingState = getBufferingState()
            )
            is MediaPlaybackState.Empty -> MediaPlayerState.Ready(
                mediaPlaybackState = mediaPlaybackState
            )
        }
    }

    fun createMediaPlaybackState(): MediaPlaybackState<M> {
        val queueItems = queue.queueItems
        return if (queueItems == null) {
            MediaPlaybackState.Empty(
                repeatMode = getRepeatMode(),
                shuffleMode = ShuffleDisabled,
                playbackSpeed = exoPlayer.playbackParameters.speed
            )
        } else {
            MediaPlaybackState.Populated(
                status = when {
                    exoPlayer.playbackState == STATE_ENDED ||
                        exoPlayer.isAtEndOfTimeline() -> Paused(reachedEndOfQueue = true)
                    exoPlayer.playWhenReady -> Playing
                    else -> Paused(reachedEndOfQueue = false)
                },
                seekPosition = when {
                    exoPlayer.playWhenReady && exoPlayer.playbackState == STATE_READY -> {
                        ComputedSeekPosition(
                            originalSeekPositionMillis = exoPlayer.currentPosition,
                            maxSeekPositionMillis = exoPlayer.contentDuration
                                .takeIf { it != C.TIME_UNSET }
                                ?: Long.MAX_VALUE,
                            playbackSpeed = exoPlayer.playbackParameters.speed
                        )
                    }
                    else -> {
                        AbsoluteSeekPosition(
                            seekPositionMillis = exoPlayer.currentPosition
                        )
                    }
                },
                queue = run {
                    val currentQueueId = exoPlayer.currentMediaItem?.mediaId
                        ?.let { UUID.fromString(it) }

                    val nowPlayingIndex = queueItems.queue
                        .indexOfFirst { it.queueId == currentQueueId }
                        .takeIf { it >= 0 } ?: 0

                    when (queueItems) {
                        is LinearQueueItems -> Linear(
                            queue = queueItems.queue,
                            queueIndex = nowPlayingIndex
                        )
                        is ShuffledQueueItems -> Shuffled(
                            queue = queueItems.queue,
                            queueIndex = nowPlayingIndex,
                            linearQueue = queueItems.linearQueue
                        )
                    }
                },
                repeatMode = getRepeatMode(),
                playbackSpeed = exoPlayer.playbackParameters.speed
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
        REPEAT_MODE_OFF -> RepeatNone
        REPEAT_MODE_ONE -> RepeatOne
        REPEAT_MODE_ALL -> RepeatAll
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
