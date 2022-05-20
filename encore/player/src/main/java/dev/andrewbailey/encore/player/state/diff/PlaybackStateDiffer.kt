package dev.andrewbailey.encore.player.state.diff

import dev.andrewbailey.encore.model.MediaObject
import dev.andrewbailey.encore.player.playback.MediaQueueItems.LinearQueueItems
import dev.andrewbailey.encore.player.playback.MediaQueueItems.ShuffledQueueItems
import dev.andrewbailey.encore.player.state.MediaPlaybackState
import dev.andrewbailey.encore.player.state.QueueState.Linear
import dev.andrewbailey.encore.player.state.QueueState.Shuffled
import dev.andrewbailey.encore.player.state.SeekPosition
import dev.andrewbailey.encore.player.state.isPlaying
import dev.andrewbailey.encore.player.state.nowPlayingOrNull
import dev.andrewbailey.encore.player.state.queueIndexOrNull
import dev.andrewbailey.encore.player.state.queueStateOrNull
import dev.andrewbailey.encore.player.state.seekPositionOrNull

internal class PlaybackStateDiffer<M : MediaObject> {

    fun generateDiff(
        oldState: MediaPlaybackState<M>,
        newState: MediaPlaybackState<M>
    ): PlaybackStateDiff<M> {
        return PlaybackStateDiff(
            operations = listOfNotNull(
                generatePauseDiff(oldState, newState),
                generateQueueDiff(oldState, newState),
                generateSeekDiff(oldState, newState),
                generateRepeatDiff(oldState, newState),
                generatePlaybackSpeedDiff(oldState, newState),
                generatePlayDiff(newState)
            )
        )
    }

    private fun generatePauseDiff(
        oldState: MediaPlaybackState<M>,
        newState: MediaPlaybackState<M>
    ): PlaybackStateModification<M>? {
        val wasPlaying = oldState.isPlaying()
        val shouldPlay = newState.isPlaying()
        return if (wasPlaying && !shouldPlay) {
            SetPlaying(false)
        } else {
            null
        }
    }

    private fun generatePlayDiff(
        newState: MediaPlaybackState<M>
    ): PlaybackStateModification<M>? {
        return if (newState.isPlaying()) {
            SetPlaying(true)
        } else {
            null
        }
    }

    private fun generateRepeatDiff(
        oldState: MediaPlaybackState<M>,
        newState: MediaPlaybackState<M>
    ): PlaybackStateModification<M>? {
        return if (oldState.repeatMode != newState.repeatMode) {
            SetRepeatMode(newState.repeatMode)
        } else {
            null
        }
    }

    private fun generatePlaybackSpeedDiff(
        oldState: MediaPlaybackState<M>,
        newState: MediaPlaybackState<M>
    ): PlaybackStateModification<M>? {
        return if (oldState.playbackSpeed != newState.playbackSpeed) {
            SetPlaybackSpeed(
                speed = newState.playbackSpeed,
                pitch = 1f
            )
        } else {
            null
        }
    }

    private fun generateQueueDiff(
        oldState: MediaPlaybackState<M>,
        newState: MediaPlaybackState<M>
    ): PlaybackStateModification<M>? {
        val oldQueue = oldState.queueStateOrNull()
        val newQueue = newState.queueStateOrNull()

        return if (oldQueue != newQueue) {
            QueueModification(
                updatedQueue = when (newQueue) {
                    is Linear -> LinearQueueItems(newQueue.queue)
                    is Shuffled -> ShuffledQueueItems(newQueue.queue, newQueue.linearQueue)
                    null -> null
                },
                isSeamless = newQueue?.nowPlaying == oldQueue?.nowPlaying &&
                    (oldState.isPlaying() && newState.isPlaying())
            )
        } else {
            null
        }
    }

    private fun generateSeekDiff(
        oldState: MediaPlaybackState<M>,
        newState: MediaPlaybackState<M>
    ): PlaybackStateModification<M>? {
        val oldStateNowPlaying = oldState.nowPlayingOrNull()
        val newStateQueueIndex = newState.queueIndexOrNull() ?: return null
        val newStateNowPlaying = newState.nowPlayingOrNull() ?: return null
        val newStateSeekPosition = newState.seekPositionOrNull() ?: return null

        return when {
            oldStateNowPlaying == newStateNowPlaying &&
                newStateSeekPosition is SeekPosition.ComputedSeekPosition -> {
                null
            }
            else -> {
                TimelinePositionChange(
                    queueIndex = newStateQueueIndex,
                    seekPositionMillis = newStateSeekPosition
                        .takeIf { it !is SeekPosition.ComputedSeekPosition }
                        ?.seekPositionMillis ?: 0L
                )
            }
        }
    }

}
