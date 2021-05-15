package dev.andrewbailey.music.model

import dev.andrewbailey.encore.provider.mediastore.MediaStoreAlbum

sealed class Album {
    abstract val id: String
    abstract val name: String
    abstract val artist: Artist?
}

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
