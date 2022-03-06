package dev.andrewbailey.encore.test

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import dev.andrewbailey.encore.model.MediaSearchArguments
import dev.andrewbailey.encore.provider.MediaProvider
import dev.andrewbailey.encore.provider.MediaSearchResults

public class FakeMusicProvider(
    playbackUriFor: (String) -> String = { songId ->
        "content://media/songs/${songId.filter { it.isDigit() }}"
    }
) : MediaProvider<FakeSong> {

    public constructor(context: Context) : this(
        playbackUriFor = {
            with(context.resources) {
                Uri.Builder()
                    .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
                    .authority(getResourcePackageName(R.raw.silence))
                    .appendPath(getResourceTypeName(R.raw.silence))
                    .appendPath(getResourceEntryName(R.raw.silence))
                    .build()
                    .toString()
            }
        }
    )

    private val artists = listOf(
        FakeArtist(
            id = "author-001",
            name = "All Logic"
        ),
        FakeArtist(
            id = "author-002",
            name = "Diode Discontinuity"
        ),
        FakeArtist(
            id = "author-003",
            name = "Solitary Romance"
        ),
        FakeArtist(
            id = "author-004",
            name = "Thyself"
        )
    )

    private val albums = listOf(
        FakeAlbum(
            id = "album-001",
            name = "Oracle",
            artist = artists[0]
        ),
        FakeAlbum(
            id = "album-002",
            name = "Illogical Capacitance",
            artist = artists[1]
        ),
        FakeAlbum(
            id = "album-003",
            name = "Catalyst Reaper",
            artist = artists[1]
        ),
        FakeAlbum(
            id = "album-004",
            name = "Monsoon",
            artist = artists[2]
        ),
        FakeAlbum(
            id = "album-005",
            name = "Dynasty Homicide",
            artist = artists[3]
        )
    )

    private val songs = listOf(
        FakeSong(
            id = "song-001",
            playbackUri = playbackUriFor("song-001"),
            name = "Equator",
            artist = artists[0],
            album = albums[0]
        ),
        FakeSong(
            id = "song-002",
            playbackUri = playbackUriFor("song-002"),
            name = "Octet",
            artist = artists[0],
            album = albums[0]
        ),
        FakeSong(
            id = "song-003",
            playbackUri = playbackUriFor("song-003"),
            name = "Roundabout",
            artist = artists[0],
            album = albums[0]
        ),
        FakeSong(
            id = "song-004",
            playbackUri = playbackUriFor("song-004"),
            name = "Molecular Opulence",
            artist = artists[1],
            album = albums[1]
        ),
        FakeSong(
            id = "song-005",
            playbackUri = playbackUriFor("song-005"),
            name = "Optic Spore",
            artist = artists[1],
            album = albums[1]
        ),
        FakeSong(
            id = "song-006",
            playbackUri = playbackUriFor("song-006"),
            name = "Tertiary",
            artist = artists[1],
            album = albums[2]
        ),
        FakeSong(
            id = "song-007",
            playbackUri = playbackUriFor("song-007"),
            name = "Perimeter Cyclotron",
            artist = artists[1],
            album = albums[2]
        ),
        FakeSong(
            id = "song-008",
            playbackUri = playbackUriFor("song-008"),
            name = "Tremors",
            artist = artists[2],
            album = albums[3]
        ),
        FakeSong(
            id = "song-009",
            playbackUri = playbackUriFor("song-009"),
            name = "Infidel",
            artist = artists[2],
            album = albums[3]
        ),
        FakeSong(
            id = "song-010",
            playbackUri = playbackUriFor("song-010"),
            name = "Fortress",
            artist = artists[3],
            album = albums[4]
        ),
        FakeSong(
            id = "song-011",
            playbackUri = playbackUriFor("song-011"),
            name = "Interact",
            artist = artists[3],
            album = albums[4]
        )
    )

    override suspend fun getMediaItemById(id: String): FakeSong? {
        return getAllSongs()
            .firstOrNull { it.id == id }
    }

    override suspend fun getMediaItemsByIds(ids: List<String>): List<FakeSong> {
        return getAllSongs()
            .filter { it.id in ids }
    }

    override suspend fun searchForMediaItems(
        query: String,
        arguments: MediaSearchArguments
    ): MediaSearchResults<FakeSong> {
        return MediaSearchResults(
            searchResults = getAllSongs()
                .filter { it.name.contains(query, ignoreCase = true) },
            playbackContinuation = emptyList()
        )
    }

    public suspend fun getAllSongs(): List<FakeSong> {
        return songs
    }

    public suspend fun getAllAlbums(): List<FakeAlbum> {
        return albums
    }

    public suspend fun getAlbumById(id: String): FakeAlbum? {
        return getAllAlbums()
            .firstOrNull { it.id == id }
    }

    public suspend fun getSongsInAlbum(album: FakeAlbum): List<FakeSong> {
        return getAllSongs()
            .filter { it.album == album }
    }

    public suspend fun getAllArtists(): List<FakeArtist> {
        return artists
    }

    public suspend fun getArtistById(id: String): FakeArtist? {
        return getAllArtists()
            .firstOrNull { it.id == id }
    }

    public suspend fun getAlbumsByArtist(artist: FakeArtist): List<FakeAlbum> {
        return getAllAlbums()
            .filter { it.artist == artist }
    }

    public suspend fun getSongsByArtist(artist: FakeArtist): List<FakeSong> {
        return getAllSongs()
            .filter { it.artist == artist }
    }

}
