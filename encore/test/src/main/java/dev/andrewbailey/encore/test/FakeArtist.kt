package dev.andrewbailey.encore.test

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
public data class FakeArtist(
    val id: String,
    val name: String
) : Parcelable
