package dev.andrewbailey.music.ui.library

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.andrewbailey.music.R
import dev.andrewbailey.music.ui.layout.LibraryPageLayout
import dev.andrewbailey.music.ui.library.albums.AllAlbumsRoot
import dev.andrewbailey.music.ui.library.artists.AllArtistsRoot
import dev.andrewbailey.music.ui.library.playlists.AllPlaylistsRoot
import dev.andrewbailey.music.ui.library.songs.AllSongsRoot
import dev.andrewbailey.music.ui.navigation.LibraryPage
import dev.andrewbailey.music.ui.navigation.PageTransition
import dev.andrewbailey.music.util.consume

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
        content = { paddingValues ->
            LibraryContent(
                page = selectedPage.value,
                contentPadding = paddingValues
            )
        }
    )
}

@Composable
private fun LibraryContent(
    page: LibraryPage,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    Column(
        modifier = modifier
    ) {
        LibraryAppBar(
            title = { Text(stringResource(R.string.app_name)) },
            padding = contentPadding.consume(bottom = Dp.Infinity)
        )

        Surface(Modifier.weight(1f)) {
            PageTransition(
                targetState = page,
                slidePercentage = 0.04f,
                modifier = Modifier.fillMaxSize()
            ) { targetPage ->
                when (targetPage) {
                    LibraryPage.Playlists -> AllPlaylistsRoot(
                        contentPadding = contentPadding.consume(top = Dp.Infinity)
                    )
                    LibraryPage.Songs -> AllSongsRoot(
                        contentPadding = contentPadding.consume(top = Dp.Infinity)
                    )
                    LibraryPage.Albums -> AllAlbumsRoot(
                        contentPadding = contentPadding.consume(top = Dp.Infinity)
                    )
                    LibraryPage.Artists -> AllArtistsRoot(
                        contentPadding = contentPadding.consume(top = Dp.Infinity)
                    )
                    else -> {
                        Box(
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Not yet implemented.")
                        }
                    }
                }
            }
        }
    }
}
