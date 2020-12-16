package dev.andrewbailey.encore.provider.mediastore

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
public data class LocalAlbum(
    val id: String,
    val name: String,
    val author: LocalArtist?
) : Parcelable
