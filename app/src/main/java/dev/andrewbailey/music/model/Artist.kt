package dev.andrewbailey.music.model

import dev.andrewbailey.encore.provider.mediastore.MediaStoreArtist

sealed class Artist {
    abstract val id: String
    abstract val name: String
}

class LocalArtist(
    val mediaStoreArtist: MediaStoreArtist
) : Artist() {
    override val id: String
        get() = mediaStoreArtist.id

    override val name: String
        get() = mediaStoreArtist.name
}
