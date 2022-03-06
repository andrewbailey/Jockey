package dev.andrewbailey.encore.player.os

import android.net.Uri
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.session.MediaSessionCompat
import dev.andrewbailey.encore.model.QueueItem
import dev.andrewbailey.encore.player.state.QueueState

internal class QueueItemMapper {

    fun toQueue(
        playbackState: QueueState<*>
    ): List<MediaSessionCompat.QueueItem> {
        return playbackState.queue.mapIndexed { index, queueItem ->
            MediaSessionCompat.QueueItem(
                toMediaDescription(queueItem),
                index.toLong()
            )
        }
    }

    private fun toMediaDescription(item: QueueItem<*>): MediaDescriptionCompat {
        val metadata = item.mediaItem.toMediaMetadata()
        return MediaDescriptionCompat.Builder()
            .setTitle(metadata.title)
            .setSubtitle(metadata.subtitle)
            .setDescription(metadata.description)
            .setMediaId(item.mediaItem.id)
            .setMediaUri(Uri.parse(item.mediaItem.playbackUri))
            .setIconUri(metadata.artworkUri?.let { Uri.parse(it) })
            .build()
    }

}
