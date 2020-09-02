package dev.andrewbailey.encore.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
public data class MediaItem(
    val id: String,
    val playbackUri: String,
    val name: String,
    val author: MediaAuthor?,
    val collection: MediaCollection?
) : Parcelable
