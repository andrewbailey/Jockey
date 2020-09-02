package dev.andrewbailey.encore.player.state

import android.os.Parcelable
import dev.andrewbailey.encore.model.QueueItem
import dev.andrewbailey.encore.player.util.equalsIgnoringOrder
import dev.andrewbailey.encore.player.util.isUniqueBy
import kotlinx.android.parcel.Parcelize

public sealed class QueueState : Parcelable {

    public abstract val queue: List<QueueItem>
    public abstract val queueIndex: Int

    public val nowPlaying: QueueItem
        get() = queue[queueIndex]

    protected fun assertPreconditions() {
        require(queue.isNotEmpty()) {
            "Queue cannot be empty"
        }

        require(queue.isUniqueBy { it.queueId }) {
            "Every queueItem must have a unique UUID"
        }

        require(queueIndex in queue.indices) {
            "queueIndex must be in the range of ${queue.indices} (was $queueIndex)"
        }
    }

    @Parcelize
    public data class Linear(
        override val queue: List<QueueItem>,
        override val queueIndex: Int
    ) : QueueState() {

        init {
            assertPreconditions()
        }

    }

    @Parcelize
    public data class Shuffled(
        override val queue: List<QueueItem>,
        override val queueIndex: Int,
        val linearQueue: List<QueueItem>
    ) : QueueState() {

        init {
            assertPreconditions()

            require(linearQueue.equalsIgnoringOrder(queue)) {
                "linearQueue must contain all the items from the shuffled queue"
            }
        }

    }

}

public fun QueueState.copy(
    queue: List<QueueItem> = this.queue,
    queueIndex: Int = this.queueIndex
): QueueState {
    return when (this) {
        is QueueState.Linear -> copy(queue, queueIndex)
        is QueueState.Shuffled -> copy(queue, queueIndex)
    }
}
