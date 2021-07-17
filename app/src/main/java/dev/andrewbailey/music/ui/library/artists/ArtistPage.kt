package dev.andrewbailey.music.ui.library.artists

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.andrewbailey.music.R
import dev.andrewbailey.music.model.Artist
import dev.andrewbailey.music.ui.layout.LibraryPageLayout
import dev.andrewbailey.music.ui.layout.StatusBarBackground
import dev.andrewbailey.music.ui.library.LibraryViewModel
import dev.andrewbailey.music.ui.navigation.LocalAppNavigator

@Composable
fun ArtistPage(
    artist: Artist,
    modifier: Modifier = Modifier
) {
    val libraryViewModel = viewModel<LibraryViewModel>()

    val songs by libraryViewModel.getSongsByArtist(artist).observeAsState()
    val albums by libraryViewModel.getAlbumsByArtist(artist).observeAsState()

    LibraryPageLayout(
        modifier = modifier
    ) {
        Surface(
            modifier = Modifier.fillMaxSize()
        ) {
            Column {
                StatusBarBackground()
                ArtistTopAppBar(artist)

                ArtistContent(
                    artist = artist,
                    songs = songs,
                    albums = albums
                )
            }
        }
    }
}

@Composable
private fun ArtistTopAppBar(
    artist: Artist,
    modifier: Modifier = Modifier,
) {
    val navigator = LocalAppNavigator.current

    TopAppBar(
        modifier = modifier,
        title = {
            Text(
                text = artist.name,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = {
            IconButton(onClick = { navigator.pop() }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = stringResource(R.string.content_description_navigate_up)
                )
            }
        }
    )
}
