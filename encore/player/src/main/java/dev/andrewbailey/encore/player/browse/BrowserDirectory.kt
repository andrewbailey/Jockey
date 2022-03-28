package dev.andrewbailey.encore.player.browse

import dev.andrewbailey.encore.model.MediaObject
import dev.andrewbailey.encore.player.browse.BrowserDirectory.DirectoryListing.DynamicPath
import dev.andrewbailey.encore.player.browse.BrowserDirectory.DirectoryListing.MediaItems
import dev.andrewbailey.encore.player.browse.BrowserDirectory.DirectoryListing.StaticPath

private const val RESERVED_CHARS = "/@[]"

public class BrowserDirectory <M : MediaObject> internal constructor(
    private val path: String
) {

    private val entries = mutableListOf<DirectoryListing<M>>()

    public fun staticPath(
        id: String,
        name: String,
        pathContents: suspend BrowserDirectory<M>.() -> Unit
    ) {
        staticPath(
            path = BrowserPath(id, name),
            pathContents = pathContents
        )
    }

    public fun staticPath(
        path: BrowserPath,
        pathContents: suspend BrowserDirectory<M>.() -> Unit
    ) {
        require(entries.none { it is StaticPath && it.path.id == path.id })
        entries += StaticPath(path, pathContents)
    }

    public fun dynamicPaths(
        identifier: String,
        paths: suspend () -> List<BrowserPath>,
        pathContents: suspend BrowserDirectory<M>.(id: String) -> Unit
    ) {
        require(entries.none { it is DynamicPath && it.identifier == identifier })
        entries += DynamicPath(identifier, paths, pathContents)
    }

    public fun mediaItems(
        identifier: String,
        loadItems: suspend () -> List<M>
    ) {
        require(entries.none { it is MediaItems && it.identifier == identifier })
        entries += MediaItems(identifier, loadItems)
    }

    internal suspend fun traversePathAndLoadContents(path: String): List<BrowserHierarchyItem> {
        return traversePath(path).loadContents()
    }

    internal suspend fun traversePathAndGetMediaItems(path: String): BrowserMediaResults<M> {
        require(path.startsWith("/")) {
            "Invalid path: '$path'. Paths must begin with '/'"
        }

        val itemId = path.takeLastWhile { it != '/' }
        val parentDir = path.dropLast(itemId.length)
        val directory = traversePath(parentDir)

        val mediaItemProvider = directory.entries.filterIsInstance<MediaItems<M>>()
            .firstOrNull { it.contains(itemId) }
            ?: throw NoSuchElementException("$itemId is not a valid MediaItem in the hierarchy.")

        return BrowserMediaResults(
            mediaItems = mediaItemProvider.loadItems(),
            mediaItemId = itemId.substring(itemId.indexOf('[') + 1, itemId.lastIndexOf(']'))
        )
    }

    private suspend fun traversePath(path: String): BrowserDirectory<M> {
        require(path.startsWith("/")) {
            "Invalid path: '$path'. Paths must begin with '/'"
        }

        var directory = this
        var remainingPath = path.drop(1)
        while (remainingPath.isNotEmpty()) {
            val segment = remainingPath.takeWhile { it != '/' }
            remainingPath = remainingPath.drop(segment.length + 1)

            val child = directory.entries.firstOrNull { it.contains(segment) }

            val parentPath = "${directory.path}$segment/"
            directory = when (child) {
                is StaticPath -> child.getBrowserDirectory(parentPath)
                is DynamicPath -> child.getBrowserDirectory(segment, parentPath)
                is MediaItems -> throw IllegalStateException("$path is not a subdirectory")
                null -> throw NoSuchElementException("The browser hierarchy does not include $path")
            }
        }

        return directory
    }

    private suspend fun loadContents(): List<BrowserHierarchyItem> {
        return entries
            .flatMap { listing ->
                when (listing) {
                    is StaticPath -> {
                        listOf(
                            BrowserFolderItem(
                                id = "$path${listing.path.id}/",
                                name = listing.path.name
                            )
                        )
                    }
                    is DynamicPath -> {
                        listing.paths().map { item ->
                            BrowserFolderItem(
                                id = "$path${listing.identifier}@[${item.id}]/",
                                name = item.name
                            )
                        }
                    }
                    is MediaItems -> {
                        listing.loadItems().map { mediaItem ->
                            BrowserMediaItem(
                                id = "$path${listing.identifier}\$[${mediaItem.id}]",
                                item = mediaItem
                            )
                        }
                    }
                }
            }
    }

    public data class BrowserPath(
        val id: String,
        val name: String
    )

    private sealed class DirectoryListing<M : MediaObject> {

        abstract fun contains(id: String): Boolean

        class StaticPath<M : MediaObject>(
            val path: BrowserPath,
            val pathContents: suspend BrowserDirectory<M>.() -> Unit
        ) : DirectoryListing<M>() {

            init {
                require(RESERVED_CHARS.all { it !in path.id }) {
                    "Path IDs may not contain any of the following characters: $RESERVED_CHARS"
                }
            }

            suspend fun getBrowserDirectory(parentPath: String): BrowserDirectory<M> {
                return BrowserDirectory<M>(parentPath).apply {
                    pathContents()
                }
            }

            override fun contains(id: String): Boolean {
                return path.id == id
            }
        }

        class DynamicPath<M : MediaObject>(
            val identifier: String,
            val paths: suspend () -> List<BrowserPath>,
            val pathContents: suspend BrowserDirectory<M>.(String) -> Unit
        ) : DirectoryListing<M>() {

            private val pathRegex by lazy {
                """$identifier@\[.+]""".toRegex()
            }

            init {
                require(RESERVED_CHARS.all { it !in identifier }) {
                    "Identifiers may not contain any of the following characters: $RESERVED_CHARS"
                }
            }

            suspend fun getBrowserDirectory(
                segment: String,
                parentPath: String
            ): BrowserDirectory<M> {
                val itemId = segment.drop("$identifier@[".length).dropLast("]".length)
                return BrowserDirectory<M>(parentPath).apply {
                    pathContents(itemId)
                }
            }

            override fun contains(id: String): Boolean {
                return pathRegex.matches(id)
            }
        }

        class MediaItems<M : MediaObject>(
            val identifier: String,
            val loadItems: suspend () -> List<M>
        ) : DirectoryListing<M>() {

            init {
                require(RESERVED_CHARS.all { it !in identifier }) {
                    "Identifiers may not contain any of the following characters: $RESERVED_CHARS"
                }
            }

            private val pathRegex by lazy {
                """$identifier\$\[.+]""".toRegex()
            }

            override fun contains(id: String): Boolean {
                return pathRegex.matches(id)
            }
        }

    }

    internal class BrowserMediaResults<M : MediaObject>(
        val mediaItemId: String,
        val mediaItems: List<M>
    )

}
