package dev.andrewbailey.encore.player.state

import android.os.Parcelable
import dev.andrewbailey.encore.model.QueueItem
import dev.andrewbailey.encore.player.util.equalsIgnoringOrder
import dev.andrewbailey.encore.player.util.isUniqueBy
import kotlin.random.Random
import kotlinx.android.parcel.Parcelize

sealed class QueueState : Parcelable {

    abstract val queue: List<QueueItem>
    abstract val queueIndex: Int

    val nowPlaying: QueueItem
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
    data class Linear(
        override val queue: List<QueueItem>,
        override val queueIndex: Int
    ) : QueueState() {

        init {
            assertPreconditions()
        }

        fun shuffled(random: Random? = null): Shuffled {
            val shuffledQueue = random?.let { queue.shuffled(it) } ?: queue.shuffled()
            return Shuffled(
                linearQueue = queue,
                queue = shuffledQueue,
                queueIndex = shuffledQueue.indexOf(nowPlaying)
            )
        }

    }

    @Parcelize
    data class Shuffled(
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

        val linearQueueIndex: Int
            get() = linearQueue.indexOf(nowPlaying)

        fun unShuffled() = Linear(
            queue = linearQueue,
            queueIndex = linearQueueIndex
        )

    }

}
