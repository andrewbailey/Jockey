package dev.andrewbailey.encore.provider.mediastore

import android.os.Parcelable
import dev.andrewbailey.annotations.compose.ComposeStableClass
import kotlinx.parcelize.Parcelize

@ComposeStableClass
@Parcelize
public data class MediaStoreArtist(
    val id: String,
    val name: String
) : Parcelable
