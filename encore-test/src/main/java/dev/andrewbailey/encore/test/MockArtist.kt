package dev.andrewbailey.encore.test

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
public data class MockArtist(
    val id: String,
    val name: String
) : Parcelable
