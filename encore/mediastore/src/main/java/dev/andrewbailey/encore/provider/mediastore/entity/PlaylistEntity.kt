package dev.andrewbailey.encore.provider.mediastore.entity

import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import dev.andrewbailey.encore.provider.mediastore.getLongOrNull
import dev.andrewbailey.encore.provider.mediastore.getString

internal data class PlaylistEntity(
    val contentUri: Uri,
    val id: Long?,
    val name: String?,
    val data: String?
) {

    companion object {
        @Suppress("DEPRECATION")
        fun fromCursor(
            contentUri: Uri,
            cursor: Cursor
        ) = PlaylistEntity(
            contentUri = contentUri,
            id = cursor.getLongOrNull(MediaStore.Audio.Playlists._ID),
            name = cursor.getString(MediaStore.Audio.Playlists.NAME),
            data = cursor.getString(MediaStore.Audio.Playlists.DATA)
        )
    }

}
