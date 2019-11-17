package dev.andrewbailey.encore.provider.mediastore.entity

import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import dev.andrewbailey.encore.provider.mediastore.getString

internal data class SongEntity(
    val contentUri: Uri,
    val id: String,
    val title: String?,
    val albumName: String?,
    val albumId: String?,
    val artistName: String?,
    val artistId: String?
) {

    companion object {
        fun fromCursor(
            contentUri: Uri,
            cursor: Cursor
        ) = SongEntity(
            contentUri = contentUri,
            id = cursor.getString(MediaStore.Audio.Media._ID)!!,
            title = cursor.getString(MediaStore.Audio.Media.TITLE),
            albumName = cursor.getString(MediaStore.Audio.Media.ALBUM),
            albumId = cursor.getString(MediaStore.Audio.Media.ALBUM_ID),
            artistName = cursor.getString(MediaStore.Audio.Media.ARTIST),
            artistId = cursor.getString(MediaStore.Audio.Media.ARTIST_ID)
        )
    }

}
