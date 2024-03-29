package dev.andrewbailey.encore.player.state.factory

import dev.andrewbailey.encore.model.MediaObject
import dev.andrewbailey.encore.model.MediaSearchArguments
import dev.andrewbailey.encore.model.QueueItem
import dev.andrewbailey.encore.player.state.MediaPlaybackState
import dev.andrewbailey.encore.player.state.MediaPlaybackState.Empty
import dev.andrewbailey.encore.player.state.MediaPlaybackState.Populated
import dev.andrewbailey.encore.player.state.PlaybackStatus
import dev.andrewbailey.encore.player.state.PlaybackStatus.Paused
import dev.andrewbailey.encore.player.state.PlaybackStatus.Playing
import dev.andrewbailey.encore.player.state.QueueState
import dev.andrewbailey.encore.player.state.RepeatMode
import dev.andrewbailey.encore.player.state.SeekPosition
import dev.andrewbailey.encore.player.state.ShuffleMode
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
        state: MediaPlaybackState<M>
    ): MediaPlaybackState<M> {
        return state.modifyMediaPlaybackState(
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
        state: MediaPlaybackState<M>
    ): MediaPlaybackState<M> {
        return state.modifyMediaPlaybackState(
            status = {
                when (status) {
                    Playing -> Paused()
                    is Paused -> status
                }
            }
        )
    }

    override fun seekTo(
        state: MediaPlaybackState<M>,
        seekPositionMillis: Long
    ): MediaPlaybackState<M> {
        return state.modifyMediaPlaybackState(
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
        state: MediaPlaybackState<M>
    ): MediaPlaybackState<M> {
        return state.modifyMediaPlaybackState(
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
        state: MediaPlaybackState<M>
    ): MediaPlaybackState<M> {
        return when (state.repeatMode) {
            RepeatMode.RepeatAll -> state.modifyMediaPlaybackState(
                status = { Playing },
                seekPosition = { SeekPosition.AbsoluteSeekPosition(0) },
                queueIndex = { (queue.queueIndex + 1) % queue.queue.size }
            )
            else -> state.modifyMediaPlaybackState(
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
        state: MediaPlaybackState<M>,
        index: Int
    ): MediaPlaybackState<M> {
        return state.modifyMediaPlaybackState(
            status = { Playing },
            queueIndex = { index }
        )
    }

    override fun setShuffleMode(
        state: MediaPlaybackState<M>,
        shuffleMode: ShuffleMode
    ): MediaPlaybackState<M> {
        return when (state) {
            is Empty -> state.copy(
                shuffleMode = shuffleMode
            )
            is Populated -> state.copy(
                queue = state.queue.changeShuffleMode(shuffleMode)
            )
        }
    }

    override fun setRepeatMode(
        state: MediaPlaybackState<M>,
        repeatMode: RepeatMode
    ): MediaPlaybackState<M> {
        return when (state) {
            is Empty -> state.copy(
                repeatMode = repeatMode
            )
            is Populated -> state.copy(
                repeatMode = repeatMode
            )
        }
    }

    override fun playFromSearchResults(
        state: MediaPlaybackState<M>,
        query: String,
        beginPlayback: Boolean,
        arguments: MediaSearchArguments,
        searchResults: MediaSearchResults<M>
    ): MediaPlaybackState<M> {
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

        return Populated(
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
        state: MediaPlaybackState<M>,
        browserId: String,
        mediaItemId: String,
        mediaItems: List<M>
    ): MediaPlaybackState<M> {
        val queue = QueueState.Linear(
            queue = mediaItems.map { item ->
                QueueItem(queueId = UUID.randomUUID(), mediaItem = item)
            },
            queueIndex = mediaItems.indexOfFirst { it.id == mediaItemId }
        )

        return Populated(
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

    private inline fun MediaPlaybackState<M>.modifyMediaPlaybackState(
        status: Populated<M>.() -> PlaybackStatus = { this.status },
        seekPosition: Populated<M>.() -> SeekPosition = { this.seekPosition },
        queueIndex: Populated<M>.() -> Int = { this.queue.queueIndex }
    ): MediaPlaybackState<M> {
        return when (this) {
            is Empty -> this
            is Populated -> copy(
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
