package dev.andrewbailey.music.ui.navigation

sealed class Screen

data class RootScreen(
    val libraryPage: LibraryPage
) : Screen()

object NowPlayingScreen : Screen()
