package dev.andrewbailey.encore.provider.mediastore.entity

import android.database.Cursor
import android.provider.MediaStore
import dev.andrewbailey.encore.provider.mediastore.getLongOrNull
import dev.andrewbailey.encore.provider.mediastore.getString

internal data class GenreEntity(
    val id: Long,
    val name: String?
) {

    companion object {
        fun fromCursor(
            cursor: Cursor
        ) = GenreEntity(
            id = requireNotNull(cursor.getLongOrNull(MediaStore.Audio.Genres._ID)) {
                "A genre was returned by the cursor that had a null ID value."
            },
            name = cursor.getString(MediaStore.Audio.Genres.NAME)
        )
    }

}
