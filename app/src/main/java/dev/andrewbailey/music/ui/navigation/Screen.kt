package dev.andrewbailey.music.ui.navigation

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

sealed class Screen : Parcelable

@Parcelize
object RootScreen : Screen()

@Parcelize
object NowPlayingScreen : Screen()
