package dev.andrewbailey.music.ui.library.artists

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.andrewbailey.music.R
import dev.andrewbailey.music.model.Artist
import dev.andrewbailey.music.ui.data.LocalMediaLibrary
import dev.andrewbailey.music.ui.layout.LibraryPageLayout
import dev.andrewbailey.music.ui.library.LibraryAppBar
import dev.andrewbailey.music.ui.navigation.LocalAppNavigator
import dev.andrewbailey.music.util.consume

@Composable
fun ArtistPage(
    artist: Artist,
    modifier: Modifier = Modifier
) {
    val mediaLibrary = LocalMediaLibrary.current

    val songs by remember { mediaLibrary.getSongsByArtist(artist) }.collectAsState()
    val albums by remember { mediaLibrary.getAlbumsByArtist(artist) }.collectAsState()

    LibraryPageLayout(
        modifier = modifier
    ) { contentPadding ->
        Surface(
            modifier = Modifier.fillMaxSize()
        ) {
            Column {
                ArtistTopAppBar(
                    artist = artist,
                    padding = contentPadding.consume(bottom = Dp.Infinity)
                )

                ArtistContent(
                    artist = artist,
                    songs = songs,
                    albums = albums,
                    contentPadding = contentPadding.consume(top = Dp.Infinity)
                )
            }
        }
    }
}

@Composable
private fun ArtistTopAppBar(
    artist: Artist,
    modifier: Modifier = Modifier,
    padding: PaddingValues = PaddingValues(0.dp)
) {
    val navigator = LocalAppNavigator.current

    LibraryAppBar(
        modifier = modifier,
        padding = padding,
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
