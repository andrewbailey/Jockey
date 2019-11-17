package dev.andrewbailey.encore.provider.mediastore.entity

import android.database.Cursor
import android.provider.MediaStore
import dev.andrewbailey.encore.provider.mediastore.getString

internal data class ArtistEntity(
    val id: String,
    val name: String?
) {

    companion object {
        fun fromCursor(
            cursor: Cursor
        ) = ArtistEntity(
            id = cursor.getString(MediaStore.Audio.Artists._ID)!!,
            name = cursor.getString(MediaStore.Audio.Artists.ARTIST)
        )
    }

}
