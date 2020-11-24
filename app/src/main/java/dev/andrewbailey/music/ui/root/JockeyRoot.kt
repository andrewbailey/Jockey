package dev.andrewbailey.music.ui.root

import androidx.compose.runtime.Composable
import dev.andrewbailey.music.ui.library.LibraryRoot
import dev.andrewbailey.music.ui.navigation.AppNavigator
import dev.andrewbailey.music.ui.navigation.NowPlayingScreen
import dev.andrewbailey.music.ui.navigation.RootScreen
import dev.andrewbailey.music.ui.player.NowPlayingRoot

@Composable
fun JockeyRoot() {
    when (val currentScreen = AppNavigator.current.currentScreen) {
        is RootScreen -> LibraryRoot(currentScreen.libraryPage)
        is NowPlayingScreen -> NowPlayingRoot()
    }
}