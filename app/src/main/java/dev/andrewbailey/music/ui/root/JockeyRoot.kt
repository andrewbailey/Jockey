package dev.andrewbailey.music.ui.root

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.andrewbailey.music.ui.library.LibraryRoot
import dev.andrewbailey.music.ui.library.albums.AlbumPage
import dev.andrewbailey.music.ui.navigation.AlbumScreen
import dev.andrewbailey.music.ui.navigation.LocalAppNavigator
import dev.andrewbailey.music.ui.navigation.NowPlayingScreen
import dev.andrewbailey.music.ui.navigation.RootScreen
import dev.andrewbailey.music.ui.player.NowPlayingRoot

@Composable
fun JockeyRoot(
    modifier: Modifier = Modifier
) {
    LocalAppNavigator.current.render { currentScreen ->
        when (currentScreen) {
            is RootScreen -> LibraryRoot(
                modifier = modifier
            )
            is AlbumScreen -> AlbumPage(
                album = currentScreen.album,
                modifier = modifier
            )
            is NowPlayingScreen -> NowPlayingRoot(
                modifier = modifier
            )
        }
    }
}
