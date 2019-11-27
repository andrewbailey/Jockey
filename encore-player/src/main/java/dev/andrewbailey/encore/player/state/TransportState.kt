package dev.andrewbailey.encore.player.state

import dev.andrewbailey.encore.model.QueueItem

sealed class TransportState

object Idle : TransportState()

data class Active(
    val status: Status,
    val seekPosition: SeekPosition,
    val queue: List<QueueItem>,
    val queueIndex: Int
) : TransportState() {

    val nowPlaying: QueueItem
        get() = queue[queueIndex]

    init {
        require(queue.isNotEmpty()) {
            "Queue cannot be empty."
        }

        require(queueIndex in queue.indices) {
            "Current index must be within ${queue.indices}"
        }
    }

}

enum class Status {
    PLAYING,
    PAUSED,
    REACHED_END
}
