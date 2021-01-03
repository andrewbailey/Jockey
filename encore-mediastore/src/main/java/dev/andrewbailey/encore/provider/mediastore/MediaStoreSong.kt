package dev.andrewbailey.encore.provider.mediastore

import dev.andrewbailey.encore.model.MediaMetadata
import dev.andrewbailey.encore.model.MediaObject
import kotlinx.parcelize.Parcelize

@Parcelize
public data class MediaStoreSong(
    override val id: String,
    override val playbackUri: String,
    val name: String,
    val artist: MediaStoreArtist?,
    val album: MediaStoreAlbum?
) : MediaObject {

    override fun toMediaMetadata(): MediaMetadata = MediaMetadata(
        title = name
    )

}
