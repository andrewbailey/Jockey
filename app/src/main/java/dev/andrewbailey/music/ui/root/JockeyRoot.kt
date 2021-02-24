package dev.andrewbailey.music.ui.root

import androidx.compose.runtime.Composable
import dev.andrewbailey.music.ui.library.LibraryRoot
import dev.andrewbailey.music.ui.navigation.LocalAppNavigator
import dev.andrewbailey.music.ui.navigation.NowPlayingScreen
import dev.andrewbailey.music.ui.navigation.RootScreen
import dev.andrewbailey.music.ui.player.NowPlayingRoot

@Composable
fun JockeyRoot() {
    LocalAppNavigator.current.render { currentScreen ->
        when (currentScreen) {
            is RootScreen -> LibraryRoot()
            is NowPlayingScreen -> NowPlayingRoot()
        }
    }
}
