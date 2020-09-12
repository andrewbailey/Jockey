package dev.andrewbailey.encore.player.browse.impl

import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import androidx.media.MediaBrowserServiceCompat
import dev.andrewbailey.encore.model.MediaObject
import dev.andrewbailey.encore.player.browse.BrowserHierarchy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

internal class MediaBrowserImpl<M : MediaObject>(
    private val coroutineScope: CoroutineScope,
    private val hierarchy: BrowserHierarchy<M>
) {

    private val mapper = MediaBrowserMapper()

    fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): MediaBrowserServiceCompat.BrowserRoot {
        return MediaBrowserServiceCompat.BrowserRoot("/", null)
    }

    fun onLoadChildren(
        parentId: String,
        result: MediaBrowserServiceCompat.Result<List<MediaBrowserCompat.MediaItem>>
    ) {
        result.detach()
        coroutineScope.launch {
            val items = hierarchy.getItems(parentId)
            result.sendResult(items.map { mapper.toMediaBrowserItem(it) })
        }
    }

}
