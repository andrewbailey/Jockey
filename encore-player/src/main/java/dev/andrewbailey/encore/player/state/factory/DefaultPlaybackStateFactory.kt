package dev.andrewbailey.encore.player.state.factory

import dev.andrewbailey.encore.player.state.*
import dev.andrewbailey.encore.player.state.PlaybackState.*
import dev.andrewbailey.encore.player.state.TransportState.Active
import dev.andrewbailey.encore.player.state.TransportState.Idle
import java.util.*
import kotlin.math.min

class DefaultPlaybackStateFactory(
    private val random: Random = Random()
) : PlaybackStateFactory() {

    override fun play(state: TransportState): TransportState {
        return state.modifyTransportState(
            status = {
                when (status) {
                    PAUSED, REACHED_END -> PLAYING
                    PLAYING -> status
                }
            },
            seekPosition = {
                if (status == REACHED_END) {
                    SeekPosition.AbsoluteSeekPosition(0L)
                } else {
                    seekPosition
                }
            },
            queueIndex = {
                if (status == REACHED_END) {
                    0
                } else {
                    queue.queueIndex
                }
            }
        )
    }

    override fun pause(state: TransportState): TransportState {
        return state.modifyTransportState(
            status = {
                when (status) {
                    PLAYING -> PAUSED
                    PAUSED, REACHED_END -> status
                }
            }
        )
    }

    override fun seekTo(state: TransportState, seekPositionMillis: Long): TransportState {
        return state.modifyTransportState(
            status = {
                when (status) {
                    PLAYING, PAUSED -> status
                    REACHED_END -> PAUSED
                }
            },
            seekPosition = { SeekPosition.AbsoluteSeekPosition(seekPositionMillis) }
        )
    }

    override fun skipToPrevious(state: TransportState): TransportState {
        return state.modifyTransportState(
            status = { PLAYING },
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

    override fun skipToNext(state: TransportState): TransportState {
        return state.modifyTransportState(
            status = {
                if (queue.queueIndex == queue.queue.size - 1) {
                    REACHED_END
                } else {
                    PLAYING
                }
            },
            seekPosition = { SeekPosition.AbsoluteSeekPosition(0) },
            queueIndex = { min(queue.queueIndex + 1, queue.queue.size - 1) }
        )
    }

    override fun skipToIndex(state: TransportState, index: Int): TransportState {
        return state.modifyTransportState(
            status = { PLAYING },
            queueIndex = { index }
        )
    }

    override fun setShuffleMode(state: TransportState, shuffleMode: ShuffleMode): TransportState {
        return when (state) {
            is Idle -> state.copy(
                shuffleMode = shuffleMode
            )
            is Active -> state.copy(
                queue = state.queue.changeShuffleMode(shuffleMode)
            )
        }
    }

    override fun setRepeatMode(state: TransportState, repeatMode: RepeatMode): TransportState {
        return when (state) {
            is Idle -> state.copy(
                repeatMode = repeatMode
            )
            is Active -> state.copy(
                repeatMode = repeatMode
            )
        }
    }

    private inline fun TransportState.modifyTransportState(
        status: Active.() -> PlaybackState = { this.status },
        seekPosition: Active.() -> SeekPosition = { this.seekPosition },
        queueIndex: Active.() -> Int = { this.queue.queueIndex }
    ): TransportState {
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

    private fun QueueState.changeShuffleMode(shuffleMode: ShuffleMode): QueueState {
        return when (this) {
            is QueueState.Linear -> {
                when (shuffleMode) {
                    ShuffleMode.LINEAR -> this
                    ShuffleMode.SHUFFLED -> toShuffledQueue(random)
                }
            }
            is QueueState.Shuffled -> {
                when (shuffleMode) {
                    ShuffleMode.SHUFFLED -> this
                    ShuffleMode.LINEAR -> toLinearQueue()
                }
            }
        }
    }

    private fun QueueState.Linear.toShuffledQueue(random: Random): QueueState.Shuffled {
        val shuffledQueue = queue.toMutableList().apply {
            remove(nowPlaying)
            shuffle(random)
            add(element = nowPlaying, index = 0)
        }.toList()

        return QueueState.Shuffled(
            linearQueue = queue,
            queue = shuffledQueue,
            queueIndex = shuffledQueue.indexOf(nowPlaying)
        )
    }

    private fun QueueState.Shuffled.toLinearQueue(): QueueState.Linear {
        return QueueState.Linear(
            queue = linearQueue,
            queueIndex = linearQueue.indexOf(nowPlaying)
        )
    }

}
