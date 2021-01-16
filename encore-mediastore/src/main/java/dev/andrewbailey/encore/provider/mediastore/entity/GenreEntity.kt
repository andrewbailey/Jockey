package dev.andrewbailey.encore.provider.mediastore.entity

import android.database.Cursor
import android.provider.MediaStore
import dev.andrewbailey.encore.provider.mediastore.getLongOrNull
import dev.andrewbailey.encore.provider.mediastore.getString

internal data class GenreEntity(
    val id: Long?,
    val name: String?
) {

    companion object {
        fun fromCursor(
            cursor: Cursor
        ) = GenreEntity(
            id = cursor.getLongOrNull(MediaStore.Audio.Genres._ID),
            name = cursor.getString(MediaStore.Audio.Genres.NAME)
        )
    }

}
