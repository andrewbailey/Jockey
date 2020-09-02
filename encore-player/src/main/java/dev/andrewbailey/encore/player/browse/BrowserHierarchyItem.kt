package dev.andrewbailey.encore.player.browse

import dev.andrewbailey.encore.model.MediaItem

public sealed class BrowserHierarchyItem

public data class BrowserMediaItem(
    val id: String,
    val item: MediaItem
) : BrowserHierarchyItem()

public data class BrowserFolderItem(
    val id: String,
    val name: String
) : BrowserHierarchyItem()
