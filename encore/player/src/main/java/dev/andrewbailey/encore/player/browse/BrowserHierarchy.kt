package dev.andrewbailey.encore.player.browse

import dev.andrewbailey.encore.model.MediaObject

public class BrowserHierarchy<M : MediaObject>(
    private val hierarchy: BrowserDirectory<M>.() -> Unit
) {

    private val root: BrowserDirectory<M>
        get() = BrowserDirectory<M>("/").apply(hierarchy)

    internal suspend fun getItems(path: String): List<BrowserHierarchyItem> {
        return root.traversePathAndLoadContents(path)
    }

    internal suspend fun getMediaItems(path: String): BrowserDirectory.BrowserMediaResults<M> {
        return root.traversePathAndGetMediaItems(path)
    }

}
