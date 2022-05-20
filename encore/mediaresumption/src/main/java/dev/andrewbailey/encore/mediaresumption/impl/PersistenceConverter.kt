package dev.andrewbailey.encore.mediaresumption.impl

import dev.andrewbailey.encore.mediaresumption.impl.model.PersistedPlaybackState
import dev.andrewbailey.encore.mediaresumption.impl.model.PersistedQueueItem
import dev.andrewbailey.encore.model.MediaObject
import dev.andrewbailey.encore.model.QueueItem
import dev.andrewbailey.encore.player.state.MediaPlaybackState
import dev.andrewbailey.encore.player.state.PlaybackStatus
import dev.andrewbailey.encore.player.state.QueueState
import dev.andrewbailey.encore.player.state.SeekPosition
import dev.andrewbailey.encore.player.state.ShuffleMode
import dev.andrewbailey.encore.provider.MediaProvider

internal class PersistenceConverter<M : MediaObject>(
    private val mediaProvider: MediaProvider<M>
) {

    fun toPersistedPlaybackState(
        mediaPlaybackState: MediaPlaybackState<M>
    ): PersistedPlaybackState? {
        return when (mediaPlaybackState) {
            is MediaPlaybackState.Empty -> null
            is MediaPlaybackState.Populated -> PersistedPlaybackState(
                seekPositionMs = mediaPlaybackState.seekPosition.seekPositionMillis,
                queueIndex = mediaPlaybackState.queue.queueIndex,
                shuffleMode = mediaPlaybackState.shuffleMode,
                repeatMode = mediaPlaybackState.repeatMode,
                playbackSpeed = mediaPlaybackState.playbackSpeed
            )
        }
    }

    fun toPersistedQueueItems(
        mediaPlaybackState: MediaPlaybackState<M>
    ): List<PersistedQueueItem> {
        return when (mediaPlaybackState) {
            is MediaPlaybackState.Empty -> {
                emptyList()
            }
            is MediaPlaybackState.Populated -> {
                when (val queue = mediaPlaybackState.queue) {
                    is QueueState.Linear -> {
                        queue.queue.mapIndexed { index, queueItem ->
                            PersistedQueueItem(
                                queueId = queueItem.queueId,
                                index = index,
                                shuffledIndex = -1,
                                mediaItemId = queueItem.mediaItem.id
                            )
                        }
                    }
                    is QueueState.Shuffled -> {
                        val linearIndices = queue.linearQueue
                            .mapIndexed { index, item -> item.queueId to index }
                            .toMap()

                        queue.queue.mapIndexed { index, queueItem ->
                            PersistedQueueItem(
                                queueId = queueItem.queueId,
                                index = linearIndices.getValue(queueItem.queueId),
                                shuffledIndex = index,
                                mediaItemId = queueItem.mediaItem.id
                            )
                        }
                    }
                }
            }
        }
    }

    suspend fun toTransportState(
        persistedPlaybackState: PersistedPlaybackState,
        persistedQueueItems: List<PersistedQueueItem>
    ): MediaPlaybackState<M> {
        return when (val queue = toQueueState(persistedPlaybackState, persistedQueueItems)) {
            null -> {
                MediaPlaybackState.Empty(
                    repeatMode = persistedPlaybackState.repeatMode,
                    shuffleMode = persistedPlaybackState.shuffleMode,
                    playbackSpeed = persistedPlaybackState.playbackSpeed
                )
            }
            else -> {
                MediaPlaybackState.Populated(
                    status = PlaybackStatus.Paused(),
                    repeatMode = persistedPlaybackState.repeatMode,
                    playbackSpeed = persistedPlaybackState.playbackSpeed,
                    seekPosition = SeekPosition.AbsoluteSeekPosition(
                        seekPositionMillis = persistedPlaybackState.seekPositionMs
                    ),
                    queue = queue
                )
            }
        }
    }

    private suspend fun toQueueState(
        persistedPlaybackState: PersistedPlaybackState,
        persistedQueue: List<PersistedQueueItem>
    ): QueueState<M>? {
        val mediaItems = mediaProvider.getMediaItemsByIds(persistedQueue.map { it.mediaItemId })
            .associateBy { it.id }

        val linearQueue = persistedQueue
            .sortedBy { it.index }
            .mapNotNull { persistedQueueItem ->
                mediaItems[persistedQueueItem.mediaItemId]?.let {
                    QueueItem(
                        queueId = persistedQueueItem.queueId,
                        mediaItem = it
                    )
                }
            }.takeIf { it.isNotEmpty() }
            ?: return null

        return when (persistedPlaybackState.shuffleMode) {
            ShuffleMode.ShuffleDisabled -> {
                QueueState.Linear(
                    queueIndex = persistedPlaybackState.queueIndex,
                    queue = linearQueue
                )
            }
            ShuffleMode.ShuffleEnabled -> {
                QueueState.Shuffled(
                    queueIndex = persistedPlaybackState.queueIndex,
                    queue = persistedQueue
                        .sortedBy { it.shuffledIndex }
                        .mapNotNull { persistedQueueItem ->
                            mediaItems[persistedQueueItem.mediaItemId]?.let {
                                QueueItem(
                                    queueId = persistedQueueItem.queueId,
                                    mediaItem = it
                                )
                            }
                        },
                    linearQueue = linearQueue
                )
            }
        }
    }

    suspend fun toMediaObject(
        persistedQueueItem: PersistedQueueItem
    ): M? = mediaProvider.getMediaItemById(persistedQueueItem.mediaItemId)

}
