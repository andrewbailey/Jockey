package dev.andrewbailey.encore.test

import dev.andrewbailey.encore.model.MediaAuthor
import dev.andrewbailey.encore.model.MediaCollection
import dev.andrewbailey.encore.model.MediaItem
import dev.andrewbailey.encore.provider.MediaProvider

class MockMediaProvider : MediaProvider {

    private val artists = listOf(
        MediaAuthor(
            id = "author-001",
            name = "All Logic"
        ),
        MediaAuthor(
            id = "author-002",
            name = "Diode Discontinuity"
        ),
        MediaAuthor(
            id = "author-003",
            name = "Solitary Romance"
        ),
        MediaAuthor(
            id = "author-004",
            name = "Thyself"
        )
    )

    private val albums = listOf(
        MediaCollection(
            id = "album-001",
            name = "Oracle",
            author = artists[0]
        ),
        MediaCollection(
            id = "album-002",
            name = "Illogical Capacitance",
            author = artists[1]
        ),
        MediaCollection(
            id = "album-003",
            name = "Catalyst Reaper",
            author = artists[1]
        ),
        MediaCollection(
            id = "album-004",
            name = "Monsoon",
            author = artists[2]
        ),
        MediaCollection(
            id = "album-005",
            name = "Dynasty Homicide",
            author = artists[3]
        )
    )

    private val songs = listOf(
        MediaItem(
            id = "song-001",
            playbackUri = "content://media/songs/001",
            name = "Equator",
            author = artists[0],
            collection = albums[0]
        ),
        MediaItem(
            id = "song-002",
            playbackUri = "content://media/songs/002",
            name = "Octet",
            author = artists[0],
            collection = albums[0]
        ),
        MediaItem(
            id = "song-003",
            playbackUri = "content://media/songs/003",
            name = "Roundabout",
            author = artists[0],
            collection = albums[0]
        ),
        MediaItem(
            id = "song-004",
            playbackUri = "content://media/songs/004",
            name = "Molecular Opulence",
            author = artists[1],
            collection = albums[1]
        ),
        MediaItem(
            id = "song-005",
            playbackUri = "content://media/songs/005",
            name = "Optic Spore",
            author = artists[1],
            collection = albums[1]
        ),
        MediaItem(
            id = "song-006",
            playbackUri = "content://media/songs/006",
            name = "Tertiary",
            author = artists[1],
            collection = albums[2]
        ),
        MediaItem(
            id = "song-007",
            playbackUri = "content://media/songs/007",
            name = "Perimeter Cyclotron",
            author = artists[1],
            collection = albums[2]
        ),
        MediaItem(
            id = "song-008",
            playbackUri = "content://media/songs/008",
            name = "Tremors",
            author = artists[2],
            collection = albums[3]
        ),
        MediaItem(
            id = "song-009",
            playbackUri = "content://media/songs/009",
            name = "Infidel",
            author = artists[2],
            collection = albums[3]
        ),
        MediaItem(
            id = "song-010",
            playbackUri = "content://media/songs/010",
            name = "Fortress",
            author = artists[3],
            collection = albums[4]
        ),
        MediaItem(
            id = "song-011",
            playbackUri = "content://media/songs/011",
            name = "Interact",
            author = artists[3],
            collection = albums[4]
        )
    )

    override suspend fun getAllMedia(): List<MediaItem> {
        return songs
    }

    override suspend fun getMediaById(id: String): MediaItem? {
        return songs.firstOrNull { it.id == id }
    }

    override suspend fun getAllCollections(): List<MediaCollection> {
        return albums
    }

    override suspend fun getCollectionById(id: String): MediaCollection? {
        return albums.firstOrNull { it.id == id }
    }

    override suspend fun getMediaInCollection(collection: MediaCollection): List<MediaItem> {
        return songs.filter { it.collection == collection }
    }

    override suspend fun getAuthors(): List<MediaAuthor> {
        return artists
    }

    override suspend fun getAuthorById(id: String): MediaAuthor? {
        return artists.firstOrNull { it.id == id }
    }

    override suspend fun getCollectionsByAuthor(author: MediaAuthor): List<MediaCollection> {
        return albums.filter { it.author == author }
    }

    override suspend fun getMediaByAuthor(author: MediaAuthor): List<MediaItem> {
        return songs.filter { it.author == author }
    }

}
