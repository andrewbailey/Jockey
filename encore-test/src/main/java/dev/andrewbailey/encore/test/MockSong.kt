package dev.andrewbailey.encore.test

import dev.andrewbailey.encore.model.MediaMetadata
import dev.andrewbailey.encore.model.MediaObject
import kotlinx.android.parcel.Parcelize

@Parcelize
public data class MockSong(
    override val id: String,
    override val playbackUri: String,
    val name: String,
    val artist: MockArtist,
    val album: MockAlbum
) : MediaObject {

    override fun toMediaMetadata(): MediaMetadata {
        return MediaMetadata(
            title = name,
            artistName = artist.name,
            albumName = album.name
        )
    }

}
