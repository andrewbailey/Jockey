package dev.andrewbailey.encore.provider.mediastore

import dev.andrewbailey.encore.model.MediaMetadata
import dev.andrewbailey.encore.model.MediaObject
import kotlinx.android.parcel.Parcelize

@Parcelize
public data class LocalSong(
    override val id: String,
    override val playbackUri: String,
    val name: String,
    val artist: LocalArtist?,
    val album: LocalAlbum?
) : MediaObject {

    override fun toMediaMetadata(): MediaMetadata = MediaMetadata(
        title = name
    )

}
