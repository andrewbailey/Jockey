package dev.andrewbailey.encore.provider.mediastore.entity

import android.database.Cursor
import android.provider.MediaStore
import dev.andrewbailey.encore.provider.mediastore.getLongOrNull

internal data class GenreContentsEntity(
    val genreId: Long?,
    val songId: Long?
) {

    companion object {
        fun fromCursor(
            cursor: Cursor
        ) = GenreContentsEntity(
            genreId = cursor.getLongOrNull(MediaStore.Audio.Genres.Members.GENRE_ID),
            songId = cursor.getLongOrNull(MediaStore.Audio.Genres.Members.AUDIO_ID)
        )
    }

}
