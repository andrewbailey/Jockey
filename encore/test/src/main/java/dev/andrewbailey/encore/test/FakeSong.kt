package dev.andrewbailey.encore.test

import dev.andrewbailey.encore.model.MediaMetadata
import dev.andrewbailey.encore.model.MediaObject
import kotlinx.parcelize.Parcelize

@Parcelize
public data class FakeSong(
    override val id: String,
    override val playbackUri: String,
    val name: String,
    val artist: FakeArtist,
    val album: FakeAlbum
) : MediaObject {

    override fun toMediaMetadata(): MediaMetadata {
        return MediaMetadata(
            title = name,
            artistName = artist.name,
            albumName = album.name
        )
    }

}
