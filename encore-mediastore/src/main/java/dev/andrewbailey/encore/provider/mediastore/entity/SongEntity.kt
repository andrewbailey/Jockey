package dev.andrewbailey.encore.provider.mediastore.entity

import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import androidx.core.database.getIntOrNull
import dev.andrewbailey.encore.provider.mediastore.getIntOrNull
import dev.andrewbailey.encore.provider.mediastore.getLong
import dev.andrewbailey.encore.provider.mediastore.getLongOrNull
import dev.andrewbailey.encore.provider.mediastore.getString

internal data class SongEntity(
    val contentUri: Uri,
    val id: Long,
    val title: String?,
    val albumName: String?,
    val albumId: String?,
    val artistName: String?,
    val artistId: String?,
    val track: Int?,
    val publishYear: Int?,
    val durationMs: Long?
) {

    companion object {
        fun fromCursor(
            contentUri: Uri,
            cursor: Cursor
        ) = SongEntity(
            contentUri = contentUri,
            id = requireNotNull(cursor.getLongOrNull(MediaStore.Audio.Media._ID)) {
                "A media track was returned by the cursor that had a null ID value."
            },
            title = cursor.getString(MediaStore.Audio.Media.TITLE),
            albumName = cursor.getString(MediaStore.Audio.Media.ALBUM),
            albumId = cursor.getString(MediaStore.Audio.Media.ALBUM_ID),
            artistName = cursor.getString(MediaStore.Audio.Media.ARTIST),
            artistId = cursor.getString(MediaStore.Audio.Media.ARTIST_ID),
            track = cursor.getIntOrNull(MediaStore.Audio.Media.TRACK),
            publishYear = cursor.getIntOrNull(MediaStore.Audio.Media.YEAR),
            // noinspection inlinedApi
            durationMs = cursor.getLong(MediaStore.Audio.Media.DURATION)
        )
    }

}
