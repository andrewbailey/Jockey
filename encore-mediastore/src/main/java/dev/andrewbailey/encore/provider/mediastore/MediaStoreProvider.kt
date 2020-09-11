package dev.andrewbailey.encore.provider.mediastore

import android.content.Context
import android.os.Build
import android.provider.MediaStore
import dev.andrewbailey.encore.provider.MediaProvider
import dev.andrewbailey.encore.provider.mediastore.entity.AlbumEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

public class MediaStoreProvider(
    context: Context
) : MediaProvider<LocalSong> {

    private val mediaStore = MediaStoreResolver(context.applicationContext)

    override suspend fun getMediaItemsByIds(ids: List<String>): List<LocalSong> {
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

    override suspend fun searchForMediaItems(query: String): List<LocalSong> {
        return getAllSongs()
            .filter { song ->
                song.name.contains(query, true) ||
                        song.artist?.name.orEmpty().contains(query, true) ||
                        song.album?.name.orEmpty().contains(query, true)
            }
    }

    override suspend fun getMediaItemById(id: String): LocalSong? {
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

    public suspend fun getAllSongs(): List<LocalSong> {
        return withContext(Dispatchers.IO) {
            mediaStore.queryAllSongs()
                .map {
                    MediaStoreMapper.toMediaItem(
                        it
                    )
                }
        }
    }

    public suspend fun getAllAlbums(): List<LocalAlbum> {
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

    public suspend fun getAlbumById(id: String): LocalAlbum? {
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

    public suspend fun getSongsInAlbum(album: LocalAlbum): List<LocalSong> {
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

    public suspend fun getAllArtists(): List<LocalArtist> {
        return withContext(Dispatchers.IO) {
            mediaStore.queryAllArtists().map {
                MediaStoreMapper.toMediaAuthor(
                    it
                )
            }
        }
    }

    public suspend fun getArtistById(id: String): LocalArtist? {
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

    public suspend fun getAlbumsByArtist(artist: LocalArtist): List<LocalAlbum> {
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

    public suspend fun getSongsByArtist(artist: LocalArtist): List<LocalSong> {
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
