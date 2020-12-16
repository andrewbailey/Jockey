package dev.andrewbailey.encore.test

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
public data class MockAlbum(
    val id: String,
    val name: String,
    val artist: MockArtist
) : Parcelable
