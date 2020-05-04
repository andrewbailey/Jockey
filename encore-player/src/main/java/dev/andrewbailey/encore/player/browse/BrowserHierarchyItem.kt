package dev.andrewbailey.encore.player.browse

import dev.andrewbailey.encore.model.MediaItem

sealed class BrowserHierarchyItem

data class BrowserMediaItem(
    val id: String,
    val item: MediaItem
) : BrowserHierarchyItem()

data class BrowserFolderItem(
    val id: String,
    val name: String
) : BrowserHierarchyItem()
