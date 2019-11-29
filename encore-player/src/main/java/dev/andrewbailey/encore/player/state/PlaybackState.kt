package dev.andrewbailey.encore.player.state

import dev.andrewbailey.encore.model.QueueItem
import dev.andrewbailey.encore.player.state.SeekPosition.AbsoluteSeekPosition
import dev.andrewbailey.encore.player.state.Status.*
import kotlin.math.min

data class PlaybackState(
    val transportState: TransportState,
    val repeatMode: RepeatMode,
    val shuffleMode: ShuffleMode
) {

    fun play(): PlaybackState {
        return modifyTransportState(
            status = {
                when (status) {
                    PAUSED, REACHED_END -> PLAYING
                    PLAYING -> status
                }
            },
            seekPosition = {
                if (status == REACHED_END) {
                    AbsoluteSeekPosition(0L)
                } else {
                    seekPosition
                }
            },
            queueIndex = {
                if (status == REACHED_END) {
                    0
                } else {
                    queueIndex
                }
            }
        )
    }

    fun pause(): PlaybackState {
        return modifyTransportState(
            status = {
                when (status) {
                    PLAYING -> PAUSED
                    PAUSED, REACHED_END -> status
                }
            }
        )
    }

    fun seekTo(seekPositionMillis: Long): PlaybackState {
        return modifyTransportState(
            status = {
                when (status) {
                    PLAYING, PAUSED -> status
                    REACHED_END -> PAUSED
                }
            },
            seekPosition = { AbsoluteSeekPosition(seekPositionMillis) }
        )
    }

    fun skipToPrevious(): PlaybackState {
        return modifyTransportState(
            status = { PLAYING },
            seekPosition = { AbsoluteSeekPosition(0) },
            queueIndex = {
                if (seekPosition.seekPositionMillis < 5_000 && queueIndex > 0) {
                    queueIndex - 1
                } else {
                    queueIndex
                }
            }
        )
    }

    fun skipToNext(): PlaybackState {
        return modifyTransportState(
            status = {
                if (queueIndex == queue.size - 1) {
                    REACHED_END
                } else {
                    PLAYING
                }
            },
            seekPosition = { AbsoluteSeekPosition(0) },
            queueIndex = { min(queueIndex + 1, queue.size - 1) }
        )
    }

    fun skipToIndex(index: Int): PlaybackState {
        return modifyTransportState(
            status = { PLAYING },
            queueIndex = { index }
        )
    }

    private inline fun modifyTransportState(
        status: Active.() -> Status = { this.status },
        seekPosition: Active.() -> SeekPosition = { this.seekPosition },
        queue: Active.() -> List<QueueItem> = { this.queue },
        queueIndex: Active.() -> Int = { this.queueIndex }
    ): PlaybackState {
        return when (transportState) {
            is Idle -> this
            is Active -> copy(
                transportState = Active(
                    transportState.status(),
                    transportState.seekPosition(),
                    transportState.queue(),
                    transportState.queueIndex()
                )
            )
        }
    }

}
