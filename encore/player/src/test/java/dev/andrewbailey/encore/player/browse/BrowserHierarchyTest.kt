package dev.andrewbailey.encore.player.browse

import dev.andrewbailey.encore.test.FakeMusicProvider
import dev.andrewbailey.encore.test.FakeSong
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlinx.coroutines.test.runBlockingTest

class BrowserHierarchyTest {

    private val mediaProvider = FakeMusicProvider()

    @Test
    fun `empty root has no items`() = runBlockingTest {
        val hierarchy = BrowserHierarchy<FakeSong> { }

        assertEquals(
            expected = emptyList(),
            actual = hierarchy.getItems("/")
        )
    }

    @Test
    fun `flat hierarchy has media items`() = runBlockingTest {
        val mediaItems = mediaProvider.getAllSongs()

        val hierarchy = BrowserHierarchy<FakeSong> {
            mediaItems(
                identifier = "media",
                loadItems = { mediaItems }
            )
        }

        assertEquals(
            expected = mediaItems.map {
                BrowserMediaItem(
                    id = "/media$[${it.id}]",
                    item = it
                )
            },
            actual = hierarchy.getItems("/")
        )
    }

    @Test
    fun `static path has media items`() = runBlockingTest {
        val mediaItems = mediaProvider.getAllSongs()

        val hierarchy = BrowserHierarchy<FakeSong> {
            staticPath(
                id = "songs",
                name = "Songs",
                pathContents = {
                    mediaItems(
                        identifier = "song",
                        loadItems = { mediaItems }
                    )
                }
            )
        }

        assertEquals(
            expected = listOf(
                BrowserFolderItem(
                    id = "/songs/",
                    name = "Songs"
                )
            ),
            actual = hierarchy.getItems("/")
        )

        assertEquals(
            expected = mediaItems.map {
                BrowserMediaItem(
                    id = "/songs/song$[${it.id}]",
                    item = it
                )
            },
            actual = hierarchy.getItems("/songs/")
        )
    }

    @Test
    fun `dynamic paths have media items`() = runBlockingTest {
        val hierarchy = BrowserHierarchy<FakeSong> {
            dynamicPaths(
                identifier = "artist",
                paths = {
                    mediaProvider.getAllArtists().map { author ->
                        BrowserDirectory.BrowserPath(
                            id = author.id,
                            name = author.name
                        )
                    }
                },
                pathContents = { pathId ->
                    mediaItems(
                        identifier = "song",
                        loadItems = {
                            val author = requireNotNull(mediaProvider.getArtistById(pathId)) {
                                "No author with id $pathId"
                            }
                            mediaProvider.getSongsByArtist(author)
                        }
                    )
                }
            )
        }

        assertEquals(
            expected = mediaProvider.getAllArtists().map {
                BrowserFolderItem(
                    id = "/artist@[${it.id}]/",
                    name = it.name
                )
            },
            actual = hierarchy.getItems("/")
        )

        mediaProvider.getAllArtists().forEach { artist ->
            assertEquals(
                expected = mediaProvider.getSongsByArtist(artist).map {
                    BrowserMediaItem(
                        id = "/artist@[${artist.id}]/song$[${it.id}]",
                        item = it
                    )
                },
                actual = hierarchy.getItems("/artist@[${artist.id}]/")
            )
        }
    }

    @Test
    fun `complicated hierarchy has all media`() = runBlockingTest {
        val hierarchy = BrowserHierarchy<FakeSong> {
            staticPath(
                id = "authors",
                name = "Authors",
                pathContents = {
                    dynamicPaths(
                        identifier = "artist",
                        paths = {
                            mediaProvider.getAllArtists().map {
                                BrowserDirectory.BrowserPath(
                                    id = it.id,
                                    name = it.name
                                )
                            }
                        },
                        pathContents = { artistId ->
                            val artist = mediaProvider.getArtistById(artistId)!!

                            dynamicPaths(
                                identifier = "album",
                                paths = {
                                    mediaProvider.getAlbumsByArtist(artist).map {
                                        BrowserDirectory.BrowserPath(
                                            id = it.id,
                                            name = it.name
                                        )
                                    }
                                },
                                pathContents = { albumId ->
                                    val album = mediaProvider.getAlbumById(albumId)!!
                                    mediaItems(
                                        identifier = "song",
                                        loadItems = { mediaProvider.getSongsInAlbum(album) }
                                    )
                                }
                            )
                        }
                    )
                }
            )
        }

        val allAlbums = mediaProvider.getAllAlbums()

        assertFalse(allAlbums.isEmpty(), "The MediaProvider must return some albums")

        allAlbums.forEach { album ->
            val path = "/authors/artist@[${album.artist.id}]/album@[${album.id}]/"
            assertEquals(
                message = "The hierarchy did not return the correct media for album $album",
                expected = mediaProvider.getSongsInAlbum(album).map {
                    BrowserMediaItem(
                        id = "${path}song$[${it.id}]",
                        item = it
                    )
                },
                actual = hierarchy.getItems(path)
            )
        }
    }

    @Test
    fun `static path identifiers must be unique`() {
        val directory = BrowserDirectory<FakeSong>("/path/to/content/")
        directory.staticPath(
            id = "existing-static-path",
            name = "A static path",
            pathContents = {}
        )

        assertFailsWith<IllegalArgumentException> {
            directory.staticPath(
                id = "existing-static-path",
                name = "A collision with the existing path",
                pathContents = {}
            )
        }
    }

    @Test
    fun `dynamic path identifiers must be unique`() {
        val directory = BrowserDirectory<FakeSong>("/path/to/content/")
        directory.dynamicPaths(
            identifier = "an-in-use-id",
            paths = { emptyList() },
            pathContents = {}
        )

        assertFailsWith<IllegalArgumentException> {
            directory.dynamicPaths(
                identifier = "an-in-use-id",
                paths = { emptyList() },
                pathContents = {}
            )
        }
    }

    @Test
    fun `media item identifiers must be unique`() {
        val directory = BrowserDirectory<FakeSong>("/path/to/content/")
        directory.mediaItems(
            identifier = "an-in-use-id",
            loadItems = { emptyList() }
        )

        assertFailsWith<IllegalArgumentException> {
            directory.mediaItems(
                identifier = "an-in-use-id",
                loadItems = { emptyList() }
            )
        }
    }

    @Test
    fun `ambiguous paths resolve to expected types`() = runBlockingTest {
        val hierarchy = BrowserHierarchy<FakeSong> {
            mediaItems(
                identifier = "ambiguous",
                loadItems = { mediaProvider.getAllSongs() }
            )

            staticPath(
                id = "ambiguous",
                name = "A static path",
                pathContents = {
                    staticPath(
                        id = "static-contents",
                        name = "A static sub-item",
                        pathContents = {}
                    )
                }
            )

            dynamicPaths(
                identifier = "ambiguous",
                paths = {
                    listOf("Foo", "Bar").map {
                        BrowserDirectory.BrowserPath(
                            id = it,
                            name = "Directory $it"
                        )
                    }
                },
                pathContents = {
                    staticPath(
                        id = "dynamic-contents",
                        name = "A static sub-item in a dynamic path",
                        pathContents = {}
                    )
                }
            )

        }

        assertEquals(
            message = "An ambiguous id referencing a static path should load its contents",
            expected = listOf(
                BrowserFolderItem(
                    id = "/ambiguous/static-contents/",
                    name = "A static sub-item"
                )
            ),
            actual = hierarchy.getItems("/ambiguous/")
        )

        assertEquals(
            message = "An ambiguous id referencing a dynamic path should load its contents",
            expected = listOf(
                BrowserFolderItem(
                    id = "/ambiguous@[Foo]/dynamic-contents/",
                    name = "A static sub-item in a dynamic path"
                )
            ),
            actual = hierarchy.getItems("/ambiguous@[Foo]/")
        )

        assertFailsWith<IllegalStateException>(
            message = "An ambiguous id referencing a media path should not be treated as a path"
        ) {
            hierarchy.getItems("/ambiguous$[Anything]/")
        }
    }
}
