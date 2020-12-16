package dev.andrewbailey.music.ui.navigation

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed class Screen : Parcelable

@Parcelize
object RootScreen : Screen()

@Parcelize
object NowPlayingScreen : Screen()
