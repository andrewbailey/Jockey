package dev.andrewbailey.encore.player.browse

import dev.andrewbailey.encore.model.MediaItem
import dev.andrewbailey.encore.model.QueueItem
import dev.andrewbailey.encore.player.browse.BrowserDirectory.DirectoryListing.*
import dev.andrewbailey.encore.player.state.*
import java.util.*
import kotlin.NoSuchElementException

private const val RESERVED_CHARS = "/@[]"

class BrowserDirectory internal constructor(
    private val path: String
) {

    private val entries = mutableListOf<DirectoryListing>()

    fun staticPath(
        id: String,
        name: String,
        pathContents: suspend BrowserDirectory.() -> Unit
    ) {
        staticPath(
            path = BrowserPath(id, name),
            pathContents = pathContents
        )
    }

    fun staticPath(
        path: BrowserPath,
        pathContents: suspend BrowserDirectory.() -> Unit
    ) {
        require(entries.none { it is StaticPath && it.path.id == path.id })
        entries += StaticPath(path, pathContents)
    }

    fun dynamicPaths(
        identifier: String,
        paths: suspend () -> List<BrowserPath>,
        pathContents: suspend BrowserDirectory.(id: String) -> Unit
    ) {
        require(entries.none { it is DynamicPath && it.identifier == identifier })
        entries += DynamicPath(identifier, paths, pathContents)
    }

    fun mediaItems(
        identifier: String,
        loadItems: suspend () -> List<MediaItem>
    ) {
        require(entries.none { it is MediaItems && it.identifier == identifier })
        entries += MediaItems(identifier, loadItems)
    }

    internal suspend fun traversePathAndLoadContents(path: String): List<BrowserHierarchyItem> {
        return traversePath(path).loadContents()
    }

    internal suspend fun traversePathAndGetTransportState(path: String): TransportState {
        require(path.startsWith("/")) {
            "Invalid path: '$path'. Paths must begin with '/'"
        }

        val itemId = path.takeLastWhile { it != '/' }
        val parentDir = path.dropLast(itemId.length)
        return traversePath(parentDir).getTransportState(itemId)
    }

    private suspend fun traversePath(path: String): BrowserDirectory {
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

    private suspend fun getTransportState(itemId: String): TransportState {
        val mediaItemProvider = entries.filterIsInstance<MediaItems>()
            .firstOrNull { it.contains(itemId) }
            ?: throw NoSuchElementException("")

        val items = mediaItemProvider.loadItems()
        val mediaId = itemId.substring(itemId.indexOf('[') + 1, itemId.lastIndexOf(']'))

        return TransportState.Active(
            status = PlaybackState.PLAYING,
            seekPosition = SeekPosition.AbsoluteSeekPosition(0),
            queue = QueueState.Linear(
                queue = items.map { QueueItem(UUID.randomUUID(), it) },
                queueIndex = items.indexOfFirst { it.id == mediaId }
            ),
            repeatMode = RepeatMode.REPEAT_NONE
        )
    }

    data class BrowserPath(
        val id: String,
        val name: String
    )

    private sealed class DirectoryListing {

        abstract fun contains(id: String): Boolean

        class StaticPath(
            val path: BrowserPath,
            val pathContents: suspend BrowserDirectory.() -> Unit
        ) : DirectoryListing() {

            init {
                require(RESERVED_CHARS.all { it !in path.id }) {
                    "Path IDs may not contain any of the following characters: $RESERVED_CHARS"
                }
            }

            suspend fun getBrowserDirectory(parentPath: String): BrowserDirectory {
                return BrowserDirectory(parentPath).apply {
                    pathContents()
                }
            }

            override fun contains(id: String): Boolean {
                return path.id == id
            }
        }

        class DynamicPath(
            val identifier: String,
            val paths: suspend () -> List<BrowserPath>,
            val pathContents: suspend BrowserDirectory.(String) -> Unit
        ) : DirectoryListing() {

            private val pathRegex by lazy {
                """$identifier@\[.+]""".toRegex()
            }

            init {
                require(RESERVED_CHARS.all { it !in identifier }) {
                    "Identifiers may not contain any of the following characters: $RESERVED_CHARS"
                }
            }

            suspend fun getBrowserDirectory(segment: String, parentPath: String): BrowserDirectory {
                val itemId = segment.drop("$identifier@[".length).dropLast("]".length)
                return BrowserDirectory(parentPath).apply {
                    pathContents(itemId)
                }
            }

            override fun contains(id: String): Boolean {
                return pathRegex.matches(id)
            }
        }

        class MediaItems(
            val identifier: String,
            val loadItems: suspend () -> List<MediaItem>
        ) : DirectoryListing() {

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

}
