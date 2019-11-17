package dev.andrewbailey.encore.provider.mediastore

import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import dev.andrewbailey.encore.provider.mediastore.entity.AlbumEntity
import dev.andrewbailey.encore.provider.mediastore.entity.ArtistEntity
import dev.andrewbailey.encore.provider.mediastore.entity.SongEntity

internal class MediaStoreResolver(
    private val context: Context
) {

    companion object {
        private val songProjection = listOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.ARTIST_ID,
            MediaStore.Audio.Media.ARTIST
        )

        private val albumProjection = listOfNotNull(
            MediaStore.Audio.Albums._ID,
            MediaStore.Audio.Albums.ALBUM,
            MediaStore.Audio.Albums.ARTIST,
            MediaStore.Audio.Albums.ALBUM_ID,
            if (Build.VERSION.SDK_INT >= 29) {
                MediaStore.Audio.Albums.ARTIST_ID
            } else null
        )

        private val artistProjection = listOf(
            MediaStore.Audio.Artists._ID,
            MediaStore.Audio.Artists.ARTIST
        )
    }

    fun queryAllSongs(
        uri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
    ): List<SongEntity> {
        val query = context.query(
            uri = uri,
            projection = songProjection,
            selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        )

        return query?.use { cursor -> cursor.toList { SongEntity.fromCursor(uri, it) } }
            .orEmpty()
    }

    fun querySongs(
        uri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
        selection: String,
        selectionArgs: List<String> = emptyList()
    ): List<SongEntity> {
        val query = context.query(
            uri = uri,
            projection = songProjection,
            selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND ($selection)",
            selectionArgs = selectionArgs.takeIf { it.isNotEmpty() }?.toTypedArray()
        )

        return query?.use { cursor -> cursor.toList { SongEntity.fromCursor(uri, it) } }
            .orEmpty()
    }

    fun queryAllAlbums(
        uri: Uri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI
    ): List<AlbumEntity> {
        val query = context.query(
            uri = uri,
            projection = albumProjection
        )

        return query?.use { cursor -> cursor.toList { AlbumEntity.fromCursor(it) } }
            .orEmpty()
    }

    fun queryAlbums(
        uri: Uri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
        selection: String,
        selectionArgs: List<String> = emptyList()
    ): List<AlbumEntity> {
        val query = context.query(
            uri = uri,
            projection = albumProjection,
            selection = selection,
            selectionArgs = selectionArgs.takeIf { it.isNotEmpty() }?.toTypedArray()
        )

        return query?.use { cursor -> cursor.toList { AlbumEntity.fromCursor(it) } }
            .orEmpty()
    }

    fun queryAllArtists(
        uri: Uri = MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI
    ): List<ArtistEntity> {
        val query = context.query(
            uri = uri,
            projection = artistProjection
        )

        return query?.use { cursor -> cursor.toList { ArtistEntity.fromCursor(it) } }
            .orEmpty()
    }

    fun queryArtists(
        uri: Uri = MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI,
        selection: String,
        selectionArgs: List<String> = emptyList()
    ): List<ArtistEntity> {
        val query = context.query(
            uri = uri,
            projection = artistProjection,
            selection = selection,
            selectionArgs = selectionArgs.takeIf { it.isNotEmpty() }?.toTypedArray()
        )

        return query?.use { cursor -> cursor.toList { ArtistEntity.fromCursor(it) } }
            .orEmpty()
    }

}
