package dev.andrewbailey.encore.player.browse

import dev.andrewbailey.encore.model.MediaObject

public sealed class BrowserHierarchyItem

public data class BrowserMediaItem(
    val id: String,
    val item: MediaObject
) : BrowserHierarchyItem()

public data class BrowserFolderItem(
    val id: String,
    val name: String
) : BrowserHierarchyItem()
