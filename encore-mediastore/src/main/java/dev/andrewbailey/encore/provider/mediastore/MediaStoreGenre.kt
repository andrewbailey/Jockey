package dev.andrewbailey.encore.provider.mediastore

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
public data class MediaStoreGenre(
    val id: String,
    val name: String
) : Parcelable
