package dev.andrewbailey.encore.provider.mediastore

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
public data class LocalArtist(
    val id: String,
    val name: String
) : Parcelable
