package dev.andrewbailey.encore.player.state

import android.os.Parcelable
import dev.andrewbailey.annotations.compose.ComposeStableClass
import dev.andrewbailey.encore.model.MediaObject
import dev.andrewbailey.encore.model.QueueItem
import dev.andrewbailey.encore.player.util.equalsIgnoringOrder
import dev.andrewbailey.encore.player.util.isUniqueBy
import kotlinx.parcelize.Parcelize

@ComposeStableClass
public sealed class QueueState<out M : MediaObject> : Parcelable {

    public abstract val queue: List<QueueItem<M>>
    public abstract val queueIndex: Int

    public val nowPlaying: QueueItem<M>
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

    @ComposeStableClass
    @Parcelize
    public data class Linear<out M : MediaObject>(
        override val queue: List<QueueItem<M>>,
        override val queueIndex: Int
    ) : QueueState<M>() {

        init {
            assertPreconditions()
        }

    }

    @ComposeStableClass
    @Parcelize
    public data class Shuffled<out M : MediaObject>(
        override val queue: List<QueueItem<M>>,
        override val queueIndex: Int,
        val linearQueue: List<QueueItem<M>>
    ) : QueueState<M>() {

        init {
            assertPreconditions()

            require(linearQueue.equalsIgnoringOrder(queue)) {
                "linearQueue must contain all the items from the shuffled queue"
            }
        }

    }

}

public fun <M : MediaObject> QueueState<M>.copy(
    queueIndex: Int = this.queueIndex
): QueueState<M> {
    return when (this) {
        is QueueState.Linear -> copy(queueIndex = queueIndex)
        is QueueState.Shuffled -> copy(queueIndex = queueIndex)
    }
}
