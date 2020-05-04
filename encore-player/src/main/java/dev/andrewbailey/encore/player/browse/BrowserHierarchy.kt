package dev.andrewbailey.encore.player.browse

import dev.andrewbailey.encore.player.state.TransportState

class BrowserHierarchy(
    private val hierarchy: BrowserDirectory.() -> Unit
) {

    private val root: BrowserDirectory
        get() = BrowserDirectory("/").apply(hierarchy)

    internal suspend fun getItems(path: String): List<BrowserHierarchyItem> {
        return root.traversePathAndLoadContents(path)
    }

    internal suspend fun getTransportState(path: String): TransportState {
        return root.traversePathAndGetTransportState(path)
    }

}
