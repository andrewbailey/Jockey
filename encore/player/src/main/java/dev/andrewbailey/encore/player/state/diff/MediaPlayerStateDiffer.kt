package dev.andrewbailey.encore.player.state.diff

import android.graphics.Bitmap
import dev.andrewbailey.diff.DiffOperation
import dev.andrewbailey.diff.DiffOperation.Add
import dev.andrewbailey.diff.DiffOperation.AddAll
import dev.andrewbailey.diff.DiffOperation.Move
import dev.andrewbailey.diff.DiffOperation.MoveRange
import dev.andrewbailey.diff.DiffOperation.Remove
import dev.andrewbailey.diff.DiffOperation.RemoveRange
import dev.andrewbailey.diff.differenceOf
import dev.andrewbailey.encore.model.MediaObject
import dev.andrewbailey.encore.player.state.BufferingState
import dev.andrewbailey.encore.player.state.MediaPlayerState
import dev.andrewbailey.encore.player.state.QueueState
import dev.andrewbailey.encore.player.state.TransportState

internal class MediaPlayerStateDiffer<M : MediaObject> {

    fun applyDiff(
        toState: MediaPlayerState.Prepared<M>,
        diff: MediaPlayerStateDiff<M>
    ): MediaPlayerState.Prepared<M> {
        return diff.operations.fold(toState) { state, diffOperation ->
            when (diffOperation) {
                is ArtworkDiff -> state.copy(
                    artwork = diffOperation.artwork
                )
                is DurationDiff -> state.copy(
                    durationMs = diffOperation.durationMs
                )
                is BufferingStateDiff -> state.copy(
                    bufferingState = diffOperation.bufferingState
                )
                is TransportStateStatusDiff -> state.copy(
                    transportState = state.transportState.copy(
                        status = diffOperation.status
                    )
                )
                is TransportStateSeekPositionDiff -> state.copy(
                    transportState = state.transportState.copy(
                        seekPosition = diffOperation.seekPosition
                    )
                )
                is TransportStateQueueDiff -> state.copy(
                    transportState = state.transportState.copy(
                        queue = applyDiff(state.transportState.queue, diffOperation)
                    )
                )
                is TransportStateRepeatModeDiff -> state.copy(
                    transportState = state.transportState.copy(
                        repeatMode = diffOperation.repeatMode
                    )
                )
            }
        }
    }

    private fun applyDiff(
        oldQueueState: QueueState<M>,
        diff: TransportStateQueueDiff<M>
    ): QueueState<M> {
        val oldLinearQueueItems = when (oldQueueState) {
            is QueueState.Linear -> oldQueueState.queue
            is QueueState.Shuffled -> oldQueueState.linearQueue
        }

        // Matches the logic in generateDiff to use the linear queue as a starting point when going
        // from unshuffled to shuffled.
        val oldShuffleQueueItems = oldQueueState.queue

        return if (diff.shuffledQueueDiff != null) {
            QueueState.Shuffled(
                queue = diff.shuffledQueueDiff.diffOperations.applyDiff(oldShuffleQueueItems),
                linearQueue = diff.linearQueueDiff.diffOperations.applyDiff(oldLinearQueueItems),
                queueIndex = diff.queueIndex
            )
        } else {
            QueueState.Linear(
                queue = diff.linearQueueDiff.diffOperations.applyDiff(oldLinearQueueItems),
                queueIndex = diff.queueIndex
            )
        }
    }

    private fun <T> List<DiffOperation<T>>.applyDiff(receiver: List<T>): List<T> {
        val result = receiver.toMutableList()

        forEach { operation ->
            @Suppress("UNCHECKED_CAST")
            when (operation) {
                is Remove<*> -> {
                    result.removeAt(operation.index)
                }
                is RemoveRange<*> -> {
                    repeat(operation.endIndex - operation.startIndex) {
                        result.removeAt(operation.startIndex)
                    }
                }
                is Add<*> -> {
                    result.add(operation.index, operation.item as T)
                }
                is AddAll<*> -> {
                    result.addAll(operation.index, operation.items as List<T>)
                }
                is Move<*> -> {
                    result.move(operation.fromIndex, operation.toIndex)
                }
                is MoveRange<*> -> {
                    when {
                        operation.toIndex < operation.fromIndex -> {
                            (0 until operation.itemCount).forEach { item ->
                                result.move(operation.fromIndex + item, operation.toIndex + item)
                            }
                        }
                        operation.toIndex > operation.fromIndex -> {
                            repeat(operation.itemCount) {
                                result.move(operation.fromIndex, operation.toIndex)
                            }
                        }
                    }
                }
                else -> {
                    throw UnsupportedOperationException("Unknown diff operation $operation")
                }
            }
        }

        return result.toList()
    }

    private fun <T> MutableList<T>.move(fromIndex: Int, toIndex: Int) {
        add(
            element = removeAt(fromIndex),
            index = if (toIndex < fromIndex) {
                toIndex
            } else {
                toIndex - 1
            }
        )
    }

    fun generateDiff(
        toState: MediaPlayerState.Prepared<M>,
        fromState: MediaPlayerState.Prepared<M>
    ): MediaPlayerStateDiff<M> {
        return MediaPlayerStateDiff(
            operations = listOfNotNull(
                checkValues(
                    toState = toState,
                    fromState = fromState,
                    propertyToCompare = { artwork },
                    equals = { to, from ->
                        when {
                            to == null -> from == null
                            from != null -> to.sameAs(from)
                            else -> false
                        }
                    }
                ) {
                    ArtworkDiff(toState.artwork)
                },
                checkValues(toState, fromState, { durationMs }) {
                    DurationDiff(toState.durationMs)
                },
                checkValues(toState, fromState, { bufferingState }) {
                    BufferingStateDiff(toState.bufferingState)
                },
                checkValues(toState, fromState, { transportState.status }) {
                    TransportStateStatusDiff(toState.transportState.status)
                },
                checkValues(toState, fromState, { transportState.seekPosition }) {
                    TransportStateSeekPositionDiff(toState.transportState.seekPosition)
                },
                checkValues(toState, fromState, { transportState.queue }) {
                    val oldQueue = fromState.transportState.queue
                    val newQueue = toState.transportState.queue

                    TransportStateQueueDiff(
                        queueIndex = newQueue.queueIndex,
                        linearQueueDiff = differenceOf(
                            original = when (oldQueue) {
                                is QueueState.Linear -> oldQueue.queue
                                is QueueState.Shuffled -> oldQueue.linearQueue
                            },
                            updated = when (newQueue) {
                                is QueueState.Linear -> newQueue.queue
                                is QueueState.Shuffled -> newQueue.linearQueue
                            }
                        ),
                        shuffledQueueDiff = when (newQueue) {
                            is QueueState.Shuffled -> differenceOf(
                                // If going from unshuffled to shuffled, use the linear queue as a
                                // starting point to reduce the size of the diff
                                original = oldQueue.queue,
                                updated = newQueue.queue
                            )
                            else -> null
                        }
                    )
                },
                checkValues(toState, fromState, { transportState.repeatMode }) {
                    TransportStateRepeatModeDiff(toState.transportState.repeatMode)
                }
            )
        )
    }

    private inline fun <T : Any?> checkValues(
        toState: MediaPlayerState.Prepared<M>,
        fromState: MediaPlayerState.Prepared<M>,
        propertyToCompare: MediaPlayerState.Prepared<M>.() -> T,
        equals: (to: T, from: T) -> Boolean = { to, from -> to == from },
        createDiff: () -> MediaPlayerStateModification<M>?
    ): MediaPlayerStateModification<M>? {
        return if (!equals(toState.propertyToCompare(), fromState.propertyToCompare())) {
            createDiff()
        } else {
            null
        }
    }

    private fun MediaPlayerState.Prepared<M>.copy(
        transportState: TransportState.Populated<M> = this.transportState,
        artwork: Bitmap? = this.artwork,
        durationMs: Long? = this.durationMs,
        bufferingState: BufferingState = this.bufferingState
    ) = MediaPlayerState.Prepared(
        transportState = transportState,
        artwork = artwork,
        durationMs = durationMs,
        bufferingState = bufferingState
    )

}
