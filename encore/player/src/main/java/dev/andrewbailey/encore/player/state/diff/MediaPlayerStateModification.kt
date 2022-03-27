package dev.andrewbailey.encore.player.state.diff

import android.graphics.Bitmap
import android.os.Parcel
import android.os.Parcelable
import dev.andrewbailey.diff.DiffOperation
import dev.andrewbailey.diff.DiffResult
import dev.andrewbailey.encore.model.MediaObject
import dev.andrewbailey.encore.model.QueueItem
import dev.andrewbailey.encore.player.state.BufferingState
import dev.andrewbailey.encore.player.state.PlaybackStatus
import dev.andrewbailey.encore.player.state.RepeatMode
import dev.andrewbailey.encore.player.state.SeekPosition
import kotlinx.parcelize.Parcelize

internal sealed class MediaPlayerStateModification<out M : MediaObject> : Parcelable

@Parcelize
internal class ArtworkDiff(
    val artwork: Bitmap?
) : MediaPlayerStateModification<Nothing>()

@Parcelize
internal class DurationDiff(
    val durationMs: Long?
) : MediaPlayerStateModification<Nothing>()

@Parcelize
internal class BufferingStateDiff(
    val bufferingState: BufferingState
) : MediaPlayerStateModification<Nothing>()

@Parcelize
internal class TransportStateStatusDiff(
    val status: PlaybackStatus
) : MediaPlayerStateModification<Nothing>()

@Parcelize
internal class TransportStateSeekPositionDiff(
    val seekPosition: SeekPosition
) : MediaPlayerStateModification<Nothing>()

@Parcelize
internal class TransportStateRepeatModeDiff(
    val repeatMode: RepeatMode
) : MediaPlayerStateModification<Nothing>()

@Parcelize
internal class TransportStateQueueDiff<M : MediaObject>(
    val queueIndex: Int,
    val linearQueueDiff: QueueDiffOperations<M>,
    val shuffledQueueDiff: QueueDiffOperations<M>?
) : MediaPlayerStateModification<M>() {

    constructor(
        queueIndex: Int,
        linearQueueDiff: DiffResult<QueueItem<M>>,
        shuffledQueueDiff: DiffResult<QueueItem<M>>?
    ) : this(
        queueIndex = queueIndex,
        linearQueueDiff = QueueDiffOperations.from(linearQueueDiff.operations),
        shuffledQueueDiff = shuffledQueueDiff?.let { QueueDiffOperations.from(it.operations) }
    )

    @Parcelize
    class QueueDiffOperations<M : MediaObject> private constructor(
        private val operations: List<QueueDiffOperation<M>>
    ) : Parcelable {
        val diffOperations: List<DiffOperation<QueueItem<M>>>
            get() = operations.map { it.operation }

        companion object {
            fun <M : MediaObject> from(operations: List<DiffOperation<QueueItem<M>>>) =
                QueueDiffOperations(operations.map { QueueDiffOperation(it) })
        }
    }

    @JvmInline
    private value class QueueDiffOperation<M : MediaObject>(
        val operation: DiffOperation<QueueItem<M>>
    ) : Parcelable {
        constructor(parcel: Parcel) : this(
            operation = parcel.readOperation()
        )

        override fun writeToParcel(dest: Parcel, flags: Int) {
            dest.writeOperation(operation)
        }

        override fun describeContents(): Int = 0

        companion object CREATOR : Parcelable.Creator<QueueDiffOperation<*>> {
            private const val TYPE_REMOVE: Byte = 0
            private const val TYPE_REMOVE_RANGE: Byte = 1
            private const val TYPE_ADD: Byte = 2
            private const val TYPE_ADD_ALL: Byte = 3
            private const val TYPE_MOVE: Byte = 4
            private const val TYPE_MOVE_RANGE: Byte = 5

            private inline fun <reified T : Parcelable> Parcel.writeOperation(
                operation: DiffOperation<T>
            ) = when (operation) {
                is DiffOperation.Remove -> {
                    writeByte(TYPE_REMOVE)
                    writeInt(operation.index)
                    writeParcelable(operation.item, 0)
                }
                is DiffOperation.RemoveRange -> {
                    writeByte(TYPE_REMOVE_RANGE)
                    writeInt(operation.startIndex)
                    writeInt(operation.endIndex)
                }
                is DiffOperation.Add -> {
                    writeByte(TYPE_ADD)
                    writeInt(operation.index)
                    writeParcelable(operation.item, 0)
                }
                is DiffOperation.AddAll -> {
                    writeByte(TYPE_ADD_ALL)
                    writeInt(operation.index)
                    writeParcelableArray(operation.items.toTypedArray(), 0)
                }
                is DiffOperation.Move -> {
                    writeByte(TYPE_MOVE)
                    writeInt(operation.fromIndex)
                    writeInt(operation.toIndex)
                }
                is DiffOperation.MoveRange -> {
                    writeByte(TYPE_MOVE_RANGE)
                    writeInt(operation.fromIndex)
                    writeInt(operation.toIndex)
                    writeInt(operation.itemCount)
                }
            }

            private inline fun <reified T : Parcelable> Parcel.readOperation(): DiffOperation<T> {
                return when (val type = readByte()) {
                    TYPE_REMOVE -> DiffOperation.Remove(
                        index = readInt(),
                        item = readParcelable(T::class.java.classLoader)!!
                    )
                    TYPE_REMOVE_RANGE -> DiffOperation.RemoveRange(
                        startIndex = readInt(),
                        endIndex = readInt()
                    )
                    TYPE_ADD -> DiffOperation.Add(
                        index = readInt(),
                        item = readParcelable(T::class.java.classLoader)!!
                    )
                    TYPE_ADD_ALL -> DiffOperation.AddAll(
                        index = readInt(),
                        items = readParcelableArray(T::class.java.classLoader)!!.map { it as T }
                    )
                    TYPE_MOVE -> DiffOperation.Move(
                        fromIndex = readInt(),
                        toIndex = readInt()
                    )
                    TYPE_MOVE_RANGE -> DiffOperation.MoveRange(
                        fromIndex = readInt(),
                        toIndex = readInt(),
                        itemCount = readInt()
                    )
                    else -> throw IllegalArgumentException("Unexpected type: $type")
                }
            }

            override fun createFromParcel(parcel: Parcel): QueueDiffOperation<*> {
                return QueueDiffOperation<MediaObject>(parcel)
            }

            override fun newArray(size: Int): Array<QueueDiffOperation<*>?> {
                return arrayOfNulls(size)
            }
        }

    }
}
