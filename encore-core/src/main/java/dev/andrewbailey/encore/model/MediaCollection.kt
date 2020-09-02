package dev.andrewbailey.encore.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
public data class MediaCollection(
    val id: String,
    val name: String,
    val author: MediaAuthor?
) : Parcelable
