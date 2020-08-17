package dev.andrewbailey.encore.provider.mediastore

import android.content.Context
import android.os.Build
import android.provider.MediaStore
import dev.andrewbailey.encore.model.MediaAuthor
import dev.andrewbailey.encore.model.MediaCollection
import dev.andrewbailey.encore.model.MediaItem
import dev.andrewbailey.encore.provider.MediaProvider
import dev.andrewbailey.encore.provider.mediastore.entity.AlbumEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MediaStoreProvider(
    context: Context
) : MediaProvider {

    private val mediaStore =
        MediaStoreResolver(context.applicationContext)

    override suspend fun getAllMedia(): List<MediaItem> {
        return withContext(Dispatchers.IO) {
            mediaStore.queryAllSongs()
                .map {
                    MediaStoreMapper.toMediaItem(
                        it
                    )
                }
        }
    }

    override suspend fun getMediaById(id: String): MediaItem? {
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

    override suspend fun getAllCollections(): List<MediaCollection> {
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

    override suspend fun getCollectionById(id: String): MediaCollection? {
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

    override suspend fun getMediaInCollection(collection: MediaCollection): List<MediaItem> {
        return withContext(Dispatchers.IO) {
            mediaStore.querySongs(
                selection = "${MediaStore.Audio.Media.ALBUM_ID} = ?",
                selectionArgs = listOf(collection.id)
            ).map {
                MediaStoreMapper.toMediaItem(
                    it
                )
            }
        }
    }

    override suspend fun getAuthors(): List<MediaAuthor> {
        return withContext(Dispatchers.IO) {
            mediaStore.queryAllArtists().map {
                MediaStoreMapper.toMediaAuthor(
                    it
                )
            }
        }
    }

    override suspend fun getAuthorById(id: String): MediaAuthor? {
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

    override suspend fun getCollectionsByAuthor(author: MediaAuthor): List<MediaCollection> {
        return withContext(Dispatchers.IO) {
            val query = if (Build.VERSION.SDK_INT >= 29) {
                mediaStore.queryAlbums(
                    selection = "${MediaStore.Audio.Albums.ARTIST_ID} = ?",
                    selectionArgs = listOf(author.id)
                )
            } else {
                val albumIds = getMediaByAuthor(author).mapNotNull { it.author?.id }.distinct()

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
                ) { author.id }
            }
        }
    }

    override suspend fun getMediaByAuthor(author: MediaAuthor): List<MediaItem> {
        return withContext(Dispatchers.IO) {
            mediaStore.querySongs(
                selection = "${MediaStore.Audio.Media.ARTIST_ID} = ?",
                selectionArgs = listOf(author.id)
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
