package dev.andrewbailey.encore.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
public data class MediaAuthor(
    val id: String,
    val name: String
) : Parcelable
