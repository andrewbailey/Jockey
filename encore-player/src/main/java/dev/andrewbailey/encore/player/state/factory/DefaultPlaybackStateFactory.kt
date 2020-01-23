package dev.andrewbailey.encore.player.state.factory

import dev.andrewbailey.encore.model.QueueItem
import dev.andrewbailey.encore.player.state.*
import kotlin.math.min

object DefaultPlaybackStateFactory : PlaybackStateFactory() {

    override fun play(state: PlaybackState): PlaybackState {
        return state.modifyTransportState(
            status = {
                when (status) {
                    Status.PAUSED, Status.REACHED_END -> Status.PLAYING
                    Status.PLAYING -> status
                }
            },
            seekPosition = {
                if (status == Status.REACHED_END) {
                    SeekPosition.AbsoluteSeekPosition(0L)
                } else {
                    seekPosition
                }
            },
            queueIndex = {
                if (status == Status.REACHED_END) {
                    0
                } else {
                    queueIndex
                }
            }
        )
    }

    override fun pause(state: PlaybackState): PlaybackState {
        return state.modifyTransportState(
            status = {
                when (status) {
                    Status.PLAYING -> Status.PAUSED
                    Status.PAUSED, Status.REACHED_END -> status
                }
            }
        )
    }

    override fun seekTo(state: PlaybackState, seekPositionMillis: Long): PlaybackState {
        return state.modifyTransportState(
            status = {
                when (status) {
                    Status.PLAYING, Status.PAUSED -> status
                    Status.REACHED_END -> Status.PAUSED
                }
            },
            seekPosition = { SeekPosition.AbsoluteSeekPosition(seekPositionMillis) }
        )
    }

    override fun skipToPrevious(state: PlaybackState): PlaybackState {
        return state.modifyTransportState(
            status = { Status.PLAYING },
            seekPosition = { SeekPosition.AbsoluteSeekPosition(0) },
            queueIndex = {
                if (seekPosition.seekPositionMillis < 5_000 && queueIndex > 0) {
                    queueIndex - 1
                } else {
                    queueIndex
                }
            }
        )
    }

    override fun skipToNext(state: PlaybackState): PlaybackState {
        return state.modifyTransportState(
            status = {
                if (queueIndex == queue.size - 1) {
                    Status.REACHED_END
                } else {
                    Status.PLAYING
                }
            },
            seekPosition = { SeekPosition.AbsoluteSeekPosition(0) },
            queueIndex = { min(queueIndex + 1, queue.size - 1) }
        )
    }

    override fun skipToIndex(state: PlaybackState, index: Int): PlaybackState {
        return state.modifyTransportState(
            status = { Status.PLAYING },
            queueIndex = { index }
        )
    }

    private inline fun PlaybackState.modifyTransportState(
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
