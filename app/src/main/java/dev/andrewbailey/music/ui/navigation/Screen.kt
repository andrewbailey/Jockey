package dev.andrewbailey.music.ui.navigation

import android.os.Parcelable
import dev.andrewbailey.music.model.Album
import dev.andrewbailey.music.model.Artist
import kotlinx.parcelize.Parcelize

sealed class Screen : Parcelable

@Parcelize
object RootScreen : Screen()

@Parcelize
data class AlbumScreen(val album: Album) : Screen()

@Parcelize
data class ArtistScreen(val artist: Artist) : Screen()
