package dev.andrewbailey.encore.provider.mediastore.entity

import android.database.Cursor
import android.os.Build
import android.provider.MediaStore
import dev.andrewbailey.encore.provider.mediastore.getString

internal data class AlbumEntity(
    val id: String,
    val title: String?,
    val artistName: String?,
    val artistId: String?
) {

    companion object {
        fun fromCursor(
            cursor: Cursor
        ) = AlbumEntity(
            id = cursor.getString(MediaStore.Audio.Albums._ID)!!,
            title = cursor.getString(MediaStore.Audio.Albums.ALBUM),
            artistName = cursor.getString(MediaStore.Audio.Albums.ARTIST),
            artistId = if (Build.VERSION.SDK_INT >= 29) {
                cursor.getString(MediaStore.Audio.Albums.ARTIST_ID)
            } else {
                null
            }
        )
    }

}
