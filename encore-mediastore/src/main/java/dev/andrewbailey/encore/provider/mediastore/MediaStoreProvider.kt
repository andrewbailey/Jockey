package dev.andrewbailey.encore.provider.mediastore

import android.content.Context
import android.os.Build
import android.provider.MediaStore
import dev.andrewbailey.encore.model.MediaSearchArguments
import dev.andrewbailey.encore.provider.MediaField.Author
import dev.andrewbailey.encore.provider.MediaField.Collection
import dev.andrewbailey.encore.provider.MediaField.Genre
import dev.andrewbailey.encore.provider.MediaField.Title
import dev.andrewbailey.encore.provider.MediaProvider
import dev.andrewbailey.encore.provider.MediaSearchResults
import dev.andrewbailey.encore.provider.mediastore.entity.AlbumEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

public class MediaStoreProvider(
    context: Context
) : MediaProvider<MediaStoreSong> {

    private val mediaStore = MediaStoreResolver(context.applicationContext)

    override suspend fun getMediaItemsByIds(ids: List<String>): List<MediaStoreSong> {
        return withContext(Dispatchers.IO) {
            ids.chunked(MediaStoreResolver.MAX_SELECTION_ARGS)
                .flatMap { idsSubset ->
                    mediaStore.querySongs(
                        selection = idsSubset.asSequence()
                            .map { "${MediaStore.Audio.Media._ID} = ?" }
                            .joinToString(separator = " OR "),
                        selectionArgs = idsSubset
                    )
                }
                .map { MediaStoreMapper.toMediaItem(it) }
        }
    }

    override suspend fun searchForMediaItems(
        query: String,
        arguments: MediaSearchArguments
    ): MediaSearchResults<MediaStoreSong> {
        val allSongs = getAllSongs()

        val searchResults = allSongs
            .filter { song ->
                // TODO: Add genre support.
                song.name.contains(arguments.fields[Title] ?: query, true) ||
                    song.artist?.name.orEmpty().contains(arguments.fields[Author] ?: query, true) ||
                    song.album?.name.orEmpty().contains(arguments.fields[Collection] ?: query, true)
            }

        val playbackContinuation = when (arguments.preferredSearchField) {
            Title, Collection -> {
                // Include other songs by the artists in the search results
                searchResults.mapNotNull { it.artist }
                    .toSet()
                    .flatMap { getSongsByArtist(it) }
                    .filter { it !in searchResults }
            }
            Author -> emptyList() // TODO: Search for songs in the same genre.
            Genre -> emptyList()
            null -> emptyList() // TODO: Search for songs in the same genre.
        }

        // TODO: Search results should be sorted with better matches appearing first.
        return MediaSearchResults(
            searchResults = searchResults,
            playbackContinuation = playbackContinuation
        )
    }

    override suspend fun getMediaItemById(id: String): MediaStoreSong? {
        return withContext(Dispatchers.IO) {
            mediaStore.querySongs(
                selection = "${MediaStore.Audio.Media._ID} = ?",
                selectionArgs = listOf(id)
            ).firstOrNull()
                ?.let {
                    MediaStoreMapper.toMediaItem(
                        it
                    )
                }
        }
    }

    public suspend fun getAllSongs(): List<MediaStoreSong> {
        return withContext(Dispatchers.IO) {
            mediaStore.queryAllSongs()
                .map {
                    MediaStoreMapper.toMediaItem(
                        it
                    )
                }
        }
    }

    public suspend fun getAllAlbums(): List<MediaStoreAlbum> {
        return withContext(Dispatchers.IO) {
            mediaStore.queryAllAlbums()
                .map {
                    MediaStoreMapper.toMediaCollection(
                        it,
                        ::preApi29ArtistIdLookup
                    )
                }
        }
    }

    public suspend fun getAlbumById(id: String): MediaStoreAlbum? {
        return withContext(Dispatchers.IO) {
            mediaStore.queryAlbums(
                selection = "${MediaStore.Audio.Albums._ID} = ?",
                selectionArgs = listOf(id)
            ).firstOrNull()
                ?.let {
                    MediaStoreMapper.toMediaCollection(
                        it,
                        ::preApi29ArtistIdLookup
                    )
                }
        }
    }

    public suspend fun getSongsInAlbum(album: MediaStoreAlbum): List<MediaStoreSong> {
        return withContext(Dispatchers.IO) {
            mediaStore.querySongs(
                selection = "${MediaStore.Audio.Media.ALBUM_ID} = ?",
                selectionArgs = listOf(album.id)
            ).map {
                MediaStoreMapper.toMediaItem(
                    it
                )
            }
        }
    }

    public suspend fun getAllArtists(): List<MediaStoreArtist> {
        return withContext(Dispatchers.IO) {
            mediaStore.queryAllArtists().map {
                MediaStoreMapper.toMediaAuthor(
                    it
                )
            }
        }
    }

    public suspend fun getArtistById(id: String): MediaStoreArtist? {
        return withContext(Dispatchers.IO) {
            mediaStore.queryArtists(
                selection = "${MediaStore.Audio.Artists._ID} = ?",
                selectionArgs = listOf(id)
            ).firstOrNull()
                ?.let {
                    MediaStoreMapper.toMediaAuthor(
                        it
                    )
                }
        }
    }

    public suspend fun getAlbumsByArtist(artist: MediaStoreArtist): List<MediaStoreAlbum> {
        return withContext(Dispatchers.IO) {
            val query = if (Build.VERSION.SDK_INT >= 29) {
                mediaStore.queryAlbums(
                    selection = "${MediaStore.Audio.Albums.ARTIST_ID} = ?",
                    selectionArgs = listOf(artist.id)
                )
            } else {
                val albumIds = getSongsByArtist(artist).mapNotNull { it.artist?.id }.distinct()

                mediaStore.queryAlbums(
                    selection = generateSequence { "${MediaStore.Audio.Albums.ALBUM_ID} = ?" }
                        .take(albumIds.size)
                        .joinToString(separator = " OR "),
                    selectionArgs = albumIds
                )
            }

            query.map {
                MediaStoreMapper.toMediaCollection(
                    it
                ) { artist.id }
            }
        }
    }

    public suspend fun getSongsByArtist(artist: MediaStoreArtist): List<MediaStoreSong> {
        return withContext(Dispatchers.IO) {
            mediaStore.querySongs(
                selection = "${MediaStore.Audio.Media.ARTIST_ID} = ?",
                selectionArgs = listOf(artist.id)
            ).map {
                MediaStoreMapper.toMediaItem(
                    it
                )
            }
        }
    }

    private fun preApi29ArtistIdLookup(album: AlbumEntity): String? {
        return if (Build.VERSION.SDK_INT < 29) {
            val possibleIds = mediaStore.querySongs(
                selection = "${MediaStore.Audio.Media.ALBUM_ID} = ?",
                selectionArgs = listOf(album.id)
            ).mapNotNull { it.artistId }

            possibleIds.distinct()
                .associateWith { id -> possibleIds.count { id == it } }
                .maxByOrNull { (_, count) -> count }
                ?.key
        } else {
            // On API 29 and higher, we would've already determined the artist directly by looking
            // at the artist ID column. If we get here, the album must not be associated with
            // an artist.
            null
        }
    }

}
