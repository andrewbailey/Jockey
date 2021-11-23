package dev.andrewbailey.music.model

import android.os.Parcelable
import dev.andrewbailey.encore.provider.mediastore.MediaStoreArtist
import kotlinx.parcelize.Parcelize

sealed class Artist : Parcelable {
    abstract val id: String
    abstract val name: String
}

@Parcelize
class LocalArtist(
    val mediaStoreArtist: MediaStoreArtist
) : Artist() {
    override val id: String
        get() = mediaStoreArtist.id

    override val name: String
        get() = mediaStoreArtist.name
}
