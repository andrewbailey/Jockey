package dev.andrewbailey.encore.player.browse

import dev.andrewbailey.encore.model.MediaItem
import dev.andrewbailey.encore.player.state.TransportState

public class BrowserHierarchy<M : MediaItem>(
    private val hierarchy: BrowserDirectory<M>.() -> Unit
) {

    private val root: BrowserDirectory<M>
        get() = BrowserDirectory<M>("/").apply(hierarchy)

    internal suspend fun getItems(path: String): List<BrowserHierarchyItem> {
        return root.traversePathAndLoadContents(path)
    }

    internal suspend fun getTransportState(path: String): TransportState<M> {
        return root.traversePathAndGetTransportState(path)
    }

}
