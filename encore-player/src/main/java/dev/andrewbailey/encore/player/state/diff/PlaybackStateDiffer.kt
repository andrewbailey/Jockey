package dev.andrewbailey.encore.player.state.diff

import dev.andrewbailey.encore.model.QueueItem
import dev.andrewbailey.encore.player.state.Active
import dev.andrewbailey.encore.player.state.PlaybackState
import dev.andrewbailey.encore.player.state.SeekPosition
import dev.andrewbailey.encore.player.state.Status

internal class PlaybackStateDiffer {

    fun generateDiff(oldState: PlaybackState, newState: PlaybackState): PlaybackStateDiff {
        return PlaybackStateDiff(
            operations = listOfNotNull(
                generatePauseDiff(oldState, newState),
                *generateQueueDiff(oldState, newState).toTypedArray(),
                generateSeekDiff(oldState, newState),
                generatePlayDiff(newState)
            )
        )
    }

    private fun generatePauseDiff(
        oldState: PlaybackState,
        newState: PlaybackState
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
        newState: PlaybackState
    ): PlaybackStateModification? {
        return if (newState.isPlaying()) {
            SetPlaying(true)
        } else {
            null
        }
    }

    private fun generateQueueDiff(
        oldState: PlaybackState,
        newState: PlaybackState
    ): List<PlaybackStateModification> {
        val oldQueue = oldState.queue()
        val newQueue = newState.queue()
        return if (oldQueue != newQueue) {
            // TODO break this down into a list of atomic operations on the old queue
            listOf(
                StopPlayback,
                QueueModification(newQueue)
            )
        } else {
            emptyList()
        }
    }

    private fun generateSeekDiff(
        oldState: PlaybackState,
        newState: PlaybackState
    ): PlaybackStateModification? {
        val oldStateNowPlaying = oldState.nowPlaying()
        val oldStateSeekPosition = oldState.seekPosition()
        val newStateNowPlaying = newState.nowPlaying() ?: return null
        val newStateSeekPosition = newState.seekPosition() ?: return null

        return if (oldStateNowPlaying != newStateNowPlaying ||
            oldStateSeekPosition != newStateSeekPosition) {
            TimelinePositionChange(
                queueIndex = newState.nowPlayingIndex(),
                seekPositionMillis = newState.seekPosition()?.seekPositionMillis ?: 0L
            )
        } else {
            null
        }
    }

    private fun PlaybackState.isPlaying(): Boolean {
        return transportState is Active && transportState.status == Status.PLAYING
    }

    private fun PlaybackState.queue(): List<QueueItem> {
        return (transportState as? Active)?.queue.orEmpty()
    }

    private fun PlaybackState.nowPlayingIndex(): Int {
        return (transportState as? Active)?.queueIndex ?: 0
    }

    private fun PlaybackState.nowPlaying(): QueueItem? {
        return (transportState as? Active)?.nowPlaying
    }

    private fun PlaybackState.seekPosition(): SeekPosition? {
        return (transportState as? Active)?.seekPosition
    }

}
