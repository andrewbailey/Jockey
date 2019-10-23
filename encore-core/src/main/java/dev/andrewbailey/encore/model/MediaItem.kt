package dev.andrewbailey.encore.model

import android.net.Uri
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class MediaItem(
    val id: String,
    val playbackUri: Uri,
    val name: String,
    val author: MediaAuthor?,
    val collection: MediaCollection?
) : Parcelable
