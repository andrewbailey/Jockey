package dev.andrewbailey.encore.test

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
public data class MockAlbum(
    val id: String,
    val name: String,
    val artist: MockArtist
) : Parcelable
