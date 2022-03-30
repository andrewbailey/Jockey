package dev.andrewbailey.encore.player.playback

import android.content.Context
import android.os.Parcelable
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import dev.andrewbailey.diff.DiffResult
import dev.andrewbailey.diff.differenceOf
import dev.andrewbailey.encore.model.MediaObject
import dev.andrewbailey.encore.model.QueueItem
import kotlinx.parcelize.Parcelize

internal class MediaQueue<M : MediaObject>(
    context: Context,
    userAgent: String
) {

    private val mediaSourceFactory: ProgressiveMediaSource.Factory

    var queueItems: MediaQueueItems<M>? = null
        private set

    val mediaSource = ConcatenatingMediaSource()

    init {

        val dataSourceFactory = DefaultDataSource.Factory(
            context,
            DefaultHttpDataSource.Factory()
                .setUserAgent(userAgent)
        )
        mediaSourceFactory = ProgressiveMediaSource.Factory(dataSourceFactory)
    }

    fun updateQueue(newState: MediaQueueItems<M>? = null) {
        synchronized(mediaSource) {
            updateExoPlayerQueue(
                differenceOf(
                    original = queueItems?.queue.orEmpty(),
                    updated = newState?.queue.orEmpty(),
                    detectMoves = true
                )
            )

            queueItems = newState
        }
    }

    private fun updateExoPlayerQueue(operations: DiffResult<QueueItem<*>>) {
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

    private fun buildMediaSource(queueItem: QueueItem<*>): MediaSource {
        val mediaItem = MediaItem.Builder()
            .setMediaId(queueItem.queueId.toString())
            .setUri(queueItem.mediaItem.playbackUri)
            .build()
        return mediaSourceFactory.createMediaSource(mediaItem)
    }

    private fun ConcatenatingMediaSource.move(oldIndex: Int, newIndex: Int) {
        moveMediaSource(
            oldIndex,
            if (newIndex < oldIndex) {
                newIndex
            } else {
                newIndex - 1
            }
        )
    }

}

internal sealed class MediaQueueItems<M : MediaObject> : Parcelable {
    abstract val queue: List<QueueItem<M>>

    @Parcelize
    data class LinearQueueItems<M : MediaObject>(
        override val queue: List<QueueItem<M>>
    ) : MediaQueueItems<M>()

    @Parcelize
    data class ShuffledQueueItems<M : MediaObject>(
        override val queue: List<QueueItem<M>>,
        val linearQueue: List<QueueItem<M>>
    ) : MediaQueueItems<M>()
}
