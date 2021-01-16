package dev.andrewbailey.encore.provider.mediastore.entity

import android.database.Cursor
import android.provider.MediaStore
import dev.andrewbailey.encore.provider.mediastore.getLongOrNull
import dev.andrewbailey.encore.provider.mediastore.getString

internal data class AlbumEntity(
    val id: Long?,
    val title: String?,
    val artistName: String?,
    val artistId: String?
) {

    companion object {
        fun fromCursor(
            cursor: Cursor
        ) = AlbumEntity(
            id = cursor.getLongOrNull(MediaStore.Audio.Albums._ID),
            title = cursor.getString(MediaStore.Audio.Albums.ALBUM),
            artistName = cursor.getString(MediaStore.Audio.Albums.ARTIST),
            // noinspection inlinedapi
            artistId = cursor.getString(MediaStore.Audio.Albums.ARTIST_ID)
        )
    }

}
