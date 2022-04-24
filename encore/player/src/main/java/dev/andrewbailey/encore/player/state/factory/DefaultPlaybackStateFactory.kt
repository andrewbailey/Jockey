package dev.andrewbailey.encore.player.state.factory

import dev.andrewbailey.encore.model.MediaObject
import dev.andrewbailey.encore.model.MediaSearchArguments
import dev.andrewbailey.encore.model.QueueItem
import dev.andrewbailey.encore.player.state.PlaybackStatus
import dev.andrewbailey.encore.player.state.PlaybackStatus.Paused
import dev.andrewbailey.encore.player.state.PlaybackStatus.Playing
import dev.andrewbailey.encore.player.state.QueueState
import dev.andrewbailey.encore.player.state.RepeatMode
import dev.andrewbailey.encore.player.state.SeekPosition
import dev.andrewbailey.encore.player.state.ShuffleMode
import dev.andrewbailey.encore.player.state.TransportState
import dev.andrewbailey.encore.player.state.TransportState.Active
import dev.andrewbailey.encore.player.state.TransportState.Idle
import dev.andrewbailey.encore.provider.MediaSearchResults
import java.util.Random
import java.util.UUID
import kotlin.math.min

public class DefaultPlaybackStateFactory<M : MediaObject>(
    private val random: Random = Random(),
    /**
     * Controls whether or not [playFromSearchResults] will also play items returned in the
     * [MediaSearchResults.playbackContinuation] field. The default is true.
     */
    private val playFromSearchShouldIncludeContinuationResults: Boolean = true
) : PlaybackStateFactory<M>() {

    override fun play(
        state: TransportState<M>
    ): TransportState<M> {
        return state.modifyTransportState(
            status = {
                when (status) {
                    is Paused -> Playing
                    Playing -> status
                }
            },
            seekPosition = {
                if (status is Paused && status.reachedEndOfQueue) {
                    SeekPosition.AbsoluteSeekPosition(0L)
                } else {
                    seekPosition
                }
            },
            queueIndex = {
                if (status is Paused && status.reachedEndOfQueue) {
                    0
                } else {
                    queue.queueIndex
                }
            }
        )
    }

    override fun pause(
        state: TransportState<M>
    ): TransportState<M> {
        return state.modifyTransportState(
            status = {
                when (status) {
                    Playing -> Paused()
                    is Paused -> status
                }
            }
        )
    }

    override fun seekTo(
        state: TransportState<M>,
        seekPositionMillis: Long
    ): TransportState<M> {
        return state.modifyTransportState(
            status = {
                when (status) {
                    Playing -> status
                    is Paused -> Paused()
                }
            },
            seekPosition = { SeekPosition.AbsoluteSeekPosition(seekPositionMillis) }
        )
    }

    override fun skipToPrevious(
        state: TransportState<M>
    ): TransportState<M> {
        return state.modifyTransportState(
            status = { Playing },
            seekPosition = { SeekPosition.AbsoluteSeekPosition(0) },
            queueIndex = {
                if (seekPosition.seekPositionMillis < 5_000 && queue.queueIndex > 0) {
                    queue.queueIndex - 1
                } else {
                    queue.queueIndex
                }
            }
        )
    }

    override fun skipToNext(
        state: TransportState<M>
    ): TransportState<M> {
        return when (state.repeatMode) {
            RepeatMode.RepeatAll -> state.modifyTransportState(
                status = { Playing },
                seekPosition = { SeekPosition.AbsoluteSeekPosition(0) },
                queueIndex = { (queue.queueIndex + 1) % queue.queue.size }
            )
            else -> state.modifyTransportState(
                status = {
                    if (queue.queueIndex == queue.queue.size - 1) {
                        Paused()
                    } else {
                        Playing
                    }
                },
                seekPosition = {
                    if (queue.queueIndex == queue.queue.size - 1) {
                        SeekPosition.AbsoluteSeekPosition(Long.MAX_VALUE)
                    } else {
                        SeekPosition.AbsoluteSeekPosition(0)
                    }
                },
                queueIndex = { min(queue.queueIndex + 1, queue.queue.size - 1) }
            )
        }
    }

    override fun skipToIndex(
        state: TransportState<M>,
        index: Int
    ): TransportState<M> {
        return state.modifyTransportState(
            status = { Playing },
            queueIndex = { index }
        )
    }

    override fun setShuffleMode(
        state: TransportState<M>,
        shuffleMode: ShuffleMode
    ): TransportState<M> {
        return when (state) {
            is Idle -> state.copy(
                shuffleMode = shuffleMode
            )
            is Active -> state.copy(
                queue = state.queue.changeShuffleMode(shuffleMode)
            )
        }
    }

    override fun setRepeatMode(
        state: TransportState<M>,
        repeatMode: RepeatMode
    ): TransportState<M> {
        return when (state) {
            is Idle -> state.copy(
                repeatMode = repeatMode
            )
            is Active -> state.copy(
                repeatMode = repeatMode
            )
        }
    }

    override fun playFromSearchResults(
        state: TransportState<M>,
        query: String,
        beginPlayback: Boolean,
        arguments: MediaSearchArguments,
        searchResults: MediaSearchResults<M>
    ): TransportState<M> {
        if (searchResults.searchResults.isEmpty()) {
            // Do nothing if there aren't any search results.
            return state
        }

        val searchResultsQueue = searchResults.searchResults.map { searchResult ->
            QueueItem(queueId = UUID.randomUUID(), mediaItem = searchResult)
        }

        val continuationQueue = if (playFromSearchShouldIncludeContinuationResults) {
            searchResults.playbackContinuation.map { continuationItem ->
                QueueItem(queueId = UUID.randomUUID(), mediaItem = continuationItem)
            }
        } else {
            emptyList()
        }

        return Active(
            status = if (beginPlayback) Playing else Paused(),
            seekPosition = SeekPosition.AbsoluteSeekPosition(0),
            queue = when (state.shuffleMode) {
                ShuffleMode.ShuffleDisabled -> QueueState.Linear(
                    queue = searchResultsQueue + continuationQueue,
                    queueIndex = 0
                )
                ShuffleMode.ShuffleEnabled -> QueueState.Shuffled(
                    queue = searchResultsQueue.take(1)
                        .plus(searchResultsQueue.drop(1).shuffled(random))
                        .plus(continuationQueue.shuffled(random)),
                    queueIndex = 0,
                    linearQueue = searchResultsQueue + continuationQueue
                )
            },
            repeatMode = state.repeatMode,
            playbackSpeed = state.playbackSpeed
        )
    }

    override fun playFromMediaBrowser(
        state: TransportState<M>,
        browserId: String,
        mediaItemId: String,
        mediaItems: List<M>
    ): TransportState<M> {
        val queue = QueueState.Linear(
            queue = mediaItems.map { item ->
                QueueItem(queueId = UUID.randomUUID(), mediaItem = item)
            },
            queueIndex = mediaItems.indexOfFirst { it.id == mediaItemId }
        )

        return Active(
            status = Playing,
            seekPosition = SeekPosition.AbsoluteSeekPosition(0),
            queue = when (state.shuffleMode) {
                ShuffleMode.ShuffleDisabled -> queue
                ShuffleMode.ShuffleEnabled -> queue.toShuffledQueue(random)
            },
            repeatMode = state.repeatMode,
            playbackSpeed = state.playbackSpeed
        )
    }

    private inline fun TransportState<M>.modifyTransportState(
        status: Active<M>.() -> PlaybackStatus = { this.status },
        seekPosition: Active<M>.() -> SeekPosition = { this.seekPosition },
        queueIndex: Active<M>.() -> Int = { this.queue.queueIndex }
    ): TransportState<M> {
        return when (this) {
            is Idle -> this
            is Active -> copy(
                status = status(),
                seekPosition = seekPosition(),
                queue = when (queue) {
                    is QueueState.Linear -> queue.copy(
                        queueIndex = queueIndex()
                    )
                    is QueueState.Shuffled -> queue.copy(
                        queueIndex = queueIndex()
                    )
                }
            )
        }
    }

    private fun QueueState<M>.changeShuffleMode(
        shuffleMode: ShuffleMode
    ): QueueState<M> {
        return when (this) {
            is QueueState.Linear -> {
                when (shuffleMode) {
                    ShuffleMode.ShuffleDisabled -> this
                    ShuffleMode.ShuffleEnabled -> toShuffledQueue(random)
                }
            }
            is QueueState.Shuffled -> {
                when (shuffleMode) {
                    ShuffleMode.ShuffleEnabled -> this
                    ShuffleMode.ShuffleDisabled -> toLinearQueue()
                }
            }
        }
    }

    private fun QueueState.Linear<M>.toShuffledQueue(
        random: Random
    ): QueueState.Shuffled<M> {
        val shuffledQueue = buildList {
            add(nowPlaying)
            addAll(queue.filter { it != nowPlaying }.shuffled(random))
        }

        return QueueState.Shuffled(
            linearQueue = queue,
            queue = shuffledQueue,
            queueIndex = shuffledQueue.indexOf(nowPlaying)
        )
    }

    private fun QueueState.Shuffled<M>.toLinearQueue(): QueueState.Linear<M> {
        return QueueState.Linear(
            queue = linearQueue,
            queueIndex = linearQueue.indexOf(nowPlaying)
        )
    }

}
