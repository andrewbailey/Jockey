package dev.andrewbailey.encore.player.playback

import android.content.Context
import android.net.Uri
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import dev.andrewbailey.diff.DiffResult
import dev.andrewbailey.diff.differenceOf
import dev.andrewbailey.encore.model.QueueItem

internal class MediaQueue(
    context: Context,
    userAgent: String
) {

    private val mediaSourceFactory: ProgressiveMediaSource.Factory

    var queueItems: MediaQueueItems? = null
        private set

    val mediaSource = ConcatenatingMediaSource()

    init {
        val dataSourceFactory = DefaultDataSourceFactory(context, userAgent)
        mediaSourceFactory = ProgressiveMediaSource.Factory(dataSourceFactory)
    }

    fun updateQueue(newState: MediaQueueItems? = null) {
        synchronized(mediaSource) {
            updateExoPlayerQueue(differenceOf(
                original = queueItems?.queue.orEmpty(),
                updated = newState?.queue.orEmpty(),
                detectMoves = true
            ))

            queueItems = newState
        }
    }

    private fun updateExoPlayerQueue(operations: DiffResult<QueueItem>) {
        operations.applyDiff(
            remove = { index ->
                mediaSource.removeMediaSource(index)
            },
            removeRange = { start, end ->
                mediaSource.removeMediaSourceRange(start, end)
            },
            insert = { item, index ->
                mediaSource.addMediaSource(index, buildMediaSource(item))
            },
            insertAll = { items, index ->
                mediaSource.addMediaSources(index, items.map(::buildMediaSource))
            },
            move = { oldIndex, newIndex ->
                mediaSource.move(oldIndex, newIndex)
            },
            moveRange = { oldIndex, newIndex, count ->
                when {
                    newIndex < oldIndex -> {
                        (0 until count).forEach { item ->
                            val from = oldIndex + item
                            val to = newIndex + item
                            mediaSource.move(from, to)
                        }
                    }
                    newIndex > oldIndex -> {
                        repeat(count) {
                            mediaSource.move(oldIndex, newIndex)
                        }
                    }
                }
            }
        )
    }

    private fun buildMediaSource(queueItem: QueueItem): MediaSource {
        return mediaSourceFactory.createMediaSource(Uri.parse(queueItem.mediaItem.playbackUri))
    }

    private fun ConcatenatingMediaSource.move(oldIndex: Int, newIndex: Int) {
        moveMediaSource(oldIndex,
            if (newIndex < oldIndex) {
                newIndex
            } else {
                newIndex - 1
            }
        )
    }

}

internal sealed class MediaQueueItems {
    abstract val queue: List<QueueItem>

    data class LinearQueueItems(
        override val queue: List<QueueItem>
    ) : MediaQueueItems()

    data class ShuffledQueueItems(
        override val queue: List<QueueItem>,
        val linearQueue: List<QueueItem>
    ) : MediaQueueItems()
}
