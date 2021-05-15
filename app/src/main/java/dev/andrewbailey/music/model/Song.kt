package dev.andrewbailey.music.model

import dev.andrewbailey.encore.model.MediaObject
import dev.andrewbailey.encore.provider.MergedMediaObject
import dev.andrewbailey.encore.provider.mediastore.MediaStoreSong
import kotlinx.parcelize.Parcelize

sealed class Song(
    originId: String,
    delegate: MediaObject
) : MergedMediaObject(originId, delegate) {

    abstract val name: String

    abstract val artist: Artist?

    abstract val album: Album?

}

@Parcelize
data class LocalSong(
    private val mediaStoreSong: MediaStoreSong
) : Song(ORIGIN_ID, mediaStoreSong) {

    override val name: String
        get() = mediaStoreSong.name

    override val album: LocalAlbum?
        get() = mediaStoreSong.album?.let { LocalAlbum(it) }

    override val artist: LocalArtist?
        get() = mediaStoreSong.artist?.let { LocalArtist(it) }

    companion object {
        const val ORIGIN_ID = "mediaStore"
    }
}
