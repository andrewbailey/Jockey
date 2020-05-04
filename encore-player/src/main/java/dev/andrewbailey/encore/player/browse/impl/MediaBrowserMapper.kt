package dev.andrewbailey.encore.player.browse.impl

import android.net.Uri
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserCompat.MediaItem.FLAG_BROWSABLE
import android.support.v4.media.MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
import android.support.v4.media.MediaDescriptionCompat
import dev.andrewbailey.encore.player.browse.BrowserFolderItem
import dev.andrewbailey.encore.player.browse.BrowserHierarchyItem
import dev.andrewbailey.encore.player.browse.BrowserMediaItem

internal class MediaBrowserMapper {

    fun toMediaBrowserItem(
        item: BrowserHierarchyItem
    ): MediaBrowserCompat.MediaItem {
        return MediaBrowserCompat.MediaItem(
            toMediaDescription(item),
            toFlags(item)
        )
    }

    private fun toMediaDescription(
        item: BrowserHierarchyItem
    ): MediaDescriptionCompat {
        return when (item) {
            is BrowserMediaItem -> toMediaDescription(item)
            is BrowserFolderItem -> toMediaDescription(item)
        }
    }

    private fun toMediaDescription(
        item: BrowserMediaItem
    ): MediaDescriptionCompat {
        return MediaDescriptionCompat.Builder()
            .setTitle(item.item.name)
            .setMediaId(item.id)
            .setMediaUri(Uri.parse(item.item.playbackUri))
            .build()
    }

    private fun toMediaDescription(
        item: BrowserFolderItem
    ): MediaDescriptionCompat {
        return MediaDescriptionCompat.Builder()
            .setTitle(item.name)
            .setMediaId(item.id)
            .build()
    }

    private fun toFlags(item: BrowserHierarchyItem): Int {
        return when (item) {
            is BrowserMediaItem -> FLAG_PLAYABLE
            is BrowserFolderItem -> FLAG_BROWSABLE
        }
    }

}
