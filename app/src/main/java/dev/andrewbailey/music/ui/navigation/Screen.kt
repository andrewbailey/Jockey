package dev.andrewbailey.music.ui.navigation

import android.os.Parcelable
import dev.andrewbailey.music.model.Album
import kotlinx.parcelize.Parcelize

sealed class Screen : Parcelable

@Parcelize
object RootScreen : Screen()

@Parcelize
data class AlbumScreen(val album: Album) : Screen()
