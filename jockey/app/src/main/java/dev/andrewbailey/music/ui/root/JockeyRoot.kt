package dev.andrewbailey.music.ui.root

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.andrewbailey.music.ui.library.LibraryRoot
import dev.andrewbailey.music.ui.library.albums.AlbumPage
import dev.andrewbailey.music.ui.library.artists.ArtistPage
import dev.andrewbailey.music.ui.navigation.AlbumScreen
import dev.andrewbailey.music.ui.navigation.ArtistScreen
import dev.andrewbailey.music.ui.navigation.LocalAppNavigator
import dev.andrewbailey.music.ui.navigation.RootScreen

@Composable
fun JockeyRoot(
    modifier: Modifier = Modifier
) {
    with(LocalAppNavigator.current) {
        NavContent { currentScreen ->
            when (currentScreen) {
                is RootScreen -> LibraryRoot(
                    modifier = modifier
                )
                is AlbumScreen -> AlbumPage(
                    album = currentScreen.album,
                    modifier = modifier
                )
                is ArtistScreen -> ArtistPage(
                    artist = currentScreen.artist,
                    modifier = modifier
                )
            }
        }
    }
}
