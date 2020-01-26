package dev.andrewbailey.encore.player.state.diff

import dev.andrewbailey.encore.model.QueueItem
import dev.andrewbailey.encore.player.state.PlaybackState
import dev.andrewbailey.encore.player.state.SeekPosition
import dev.andrewbailey.encore.player.state.TransportState
import dev.andrewbailey.encore.player.state.TransportState.Active

internal class PlaybackStateDiffer {

    fun generateDiff(oldState: TransportState, newState: TransportState): PlaybackStateDiff {
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

    private fun generateQueueDiff(
        oldState: TransportState,
        newState: TransportState
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
        oldState: TransportState,
        newState: TransportState
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

    private fun TransportState.isPlaying(): Boolean {
        return this is Active && status == PlaybackState.PLAYING
    }

    private fun TransportState.queue(): List<QueueItem> {
        return (this as? Active)?.queue?.queue.orEmpty()
    }

    private fun TransportState.nowPlayingIndex(): Int {
        return (this as? Active)?.queue?.queueIndex ?: 0
    }

    private fun TransportState.nowPlaying(): QueueItem? {
        return (this as? Active)?.queue?.nowPlaying
    }

    private fun TransportState.seekPosition(): SeekPosition? {
        return (this as? Active)?.seekPosition
    }

}
