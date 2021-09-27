package dev.andrewbailey.music.ui.library

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import dev.andrewbailey.music.R
import dev.andrewbailey.music.ui.layout.LibraryPageLayout
import dev.andrewbailey.music.ui.layout.StatusBarBackground
import dev.andrewbailey.music.ui.library.albums.AllAlbumsRoot
import dev.andrewbailey.music.ui.library.artists.AllArtistsRoot
import dev.andrewbailey.music.ui.library.playlists.AllPlaylistsRoot
import dev.andrewbailey.music.ui.library.songs.AllSongsRoot
import dev.andrewbailey.music.ui.navigation.LibraryPage

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun LibraryRoot(
    modifier: Modifier = Modifier
) {
    val selectedPage = rememberSaveable { mutableStateOf(LibraryPage.Songs) }

    LibraryPageLayout(
        modifier = modifier,
        bottomBar = {
            LibraryNavigationBar(
                selectedPage = selectedPage.value,
                onSelectLibraryPage = { newPage ->
                    selectedPage.value = newPage
                }
            )
        },
        content = { LibraryContent(selectedPage.value) }
    )
}

@Composable
private fun LibraryContent(
    page: LibraryPage,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        StatusBarBackground()

        TopAppBar(
            title = { Text(stringResource(R.string.app_name)) }
        )

        Surface(Modifier.weight(1f)) {
            val contentModifier = Modifier.fillMaxSize()

            when (page) {
                LibraryPage.Playlists -> AllPlaylistsRoot(
                    modifier = contentModifier
                )
                LibraryPage.Songs -> AllSongsRoot(
                    modifier = contentModifier
                )
                LibraryPage.Albums -> AllAlbumsRoot(
                    modifier = contentModifier
                )
                LibraryPage.Artists -> AllArtistsRoot(
                    modifier = contentModifier
                )
                else -> {
                    Box(
                        modifier = contentModifier,
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Not yet implemented.")
                    }
                }
            }
        }
    }
}
