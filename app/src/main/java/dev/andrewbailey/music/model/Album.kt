package dev.andrewbailey.music.model

import android.os.Parcelable
import dev.andrewbailey.encore.provider.mediastore.MediaStoreAlbum
import kotlinx.parcelize.Parcelize

sealed class Album : Parcelable {
    abstract val id: String
    abstract val name: String
    abstract val artist: Artist?
}

@Parcelize
class LocalAlbum(
    val mediaStoreAlbum: MediaStoreAlbum
) : Album() {
    override val id: String
        get() = mediaStoreAlbum.id

    override val name: String
        get() = mediaStoreAlbum.name

    override val artist: LocalArtist?
        get() = mediaStoreAlbum.author?.let { LocalArtist(it) }
}
