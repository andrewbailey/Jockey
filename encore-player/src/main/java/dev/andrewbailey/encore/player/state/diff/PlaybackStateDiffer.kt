package dev.andrewbailey.encore.player.state.diff

import dev.andrewbailey.encore.model.QueueItem
import dev.andrewbailey.encore.player.playback.MediaQueueItems.*
import dev.andrewbailey.encore.player.state.PlaybackState
import dev.andrewbailey.encore.player.state.QueueState
import dev.andrewbailey.encore.player.state.QueueState.*
import dev.andrewbailey.encore.player.state.SeekPosition
import dev.andrewbailey.encore.player.state.TransportState
import dev.andrewbailey.encore.player.state.TransportState.Active

internal class PlaybackStateDiffer {

    fun generateDiff(oldState: TransportState, newState: TransportState): PlaybackStateDiff {
        return PlaybackStateDiff(
            operations = listOfNotNull(
                generatePauseDiff(oldState, newState),
                generateQueueDiff(oldState, newState),
                generateSeekDiff(oldState, newState),
                generateRepeatDiff(oldState, newState),
                generatePlayDiff(newState)
            )
        )
    }

    private fun generatePauseDiff(
        oldState: TransportState,
        newState: TransportState
    ): PlaybackStateModification? {
        val wasPlaying = oldState.isPlaying()
        val shouldPlay = newState.isPlaying()
        return if (wasPlaying && !shouldPlay) {
            SetPlaying(false)
        } else {
            null
        }
    }

    private fun generatePlayDiff(
        newState: TransportState
    ): PlaybackStateModification? {
        return if (newState.isPlaying()) {
            SetPlaying(true)
        } else {
            null
        }
    }

    private fun generateRepeatDiff(
        oldState: TransportState,
        newState: TransportState
    ): PlaybackStateModification? {
        return if (oldState.repeatMode != newState.repeatMode) {
            SetRepeatMode(newState.repeatMode)
        } else {
            null
        }
    }

    private fun generateQueueDiff(
        oldState: TransportState,
        newState: TransportState
    ): PlaybackStateModification? {
        val oldQueue = oldState.queue()
        val newQueue = newState.queue()

        return if (oldQueue != newQueue) {
            QueueModification(
                updatedQueue = when (newQueue) {
                    is Linear -> LinearQueueItems(newQueue.queue)
                    is Shuffled -> ShuffledQueueItems(newQueue.queue, newQueue.linearQueue)
                    null -> null
                },
                isSeamless = newQueue?.nowPlaying == oldQueue?.nowPlaying
            )
        } else {
            null
        }
    }

    private fun generateSeekDiff(
        oldState: TransportState,
        newState: TransportState
    ): PlaybackStateModification? {
        val oldStateNowPlaying = oldState.nowPlaying()
        val newStateQueueIndex = newState.nowPlayingIndex() ?: return null
        val newStateNowPlaying = newState.nowPlaying() ?: return null
        val newStateSeekPosition = newState.seekPosition() ?: return null

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

    private fun TransportState.isPlaying(): Boolean {
        return this is Active && status == PlaybackState.PLAYING
    }

    private fun TransportState.queue(): QueueState? {
        return (this as? Active)?.queue
    }

    private fun TransportState.nowPlayingIndex(): Int? {
        return (this as? Active)?.queue?.queueIndex
    }

    private fun TransportState.nowPlaying(): QueueItem? {
        return (this as? Active)?.queue?.nowPlaying
    }

    private fun TransportState.seekPosition(): SeekPosition? {
        return (this as? Active)?.seekPosition
    }

}
