package dev.andrewbailey.encore.player.playback

import android.content.Context
import android.net.Uri
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import dev.andrewbailey.encore.model.QueueItem

internal class MediaQueue(
    context: Context,
    userAgent: String
) {

    private val mediaSourceFactory: ProgressiveMediaSource.Factory

    var queueItems: List<QueueItem> = emptyList()
        private set

    val mediaSource = ConcatenatingMediaSource()

    init {
        val dataSourceFactory = DefaultDataSourceFactory(context, userAgent)
        mediaSourceFactory = ProgressiveMediaSource.Factory(dataSourceFactory)
    }

    fun changeQueue(queue: List<QueueItem>) {
        synchronized(mediaSource) {
            mediaSource.clear()
            mediaSource.addMediaSources(queue.map { buildMediaSource(it) })
            queueItems = queue
        }
    }

    private fun buildMediaSource(queueItem: QueueItem): MediaSource {
        return mediaSourceFactory.createMediaSource(Uri.parse(queueItem.mediaItem.playbackUri))
    }

}
