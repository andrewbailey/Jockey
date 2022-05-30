package dev.andrewbailey.music.ui.library.albums

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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.andrewbailey.music.R
import dev.andrewbailey.music.model.Album
import dev.andrewbailey.music.ui.data.LocalMediaLibrary
import dev.andrewbailey.music.ui.layout.LibraryPageLayout
import dev.andrewbailey.music.ui.library.LibraryAppBar
import dev.andrewbailey.music.ui.navigation.LocalAppNavigator
import dev.andrewbailey.music.util.consume

@Composable
fun AlbumPage(
    album: Album,
    modifier: Modifier = Modifier
) {
    val mediaLibrary = LocalMediaLibrary.current
    val songs = remember { mediaLibrary.getSongsInAlbum(album) }.collectAsState().value

    LibraryPageLayout(
        modifier = modifier
    ) { contentPadding ->
        Surface(
            modifier = Modifier.fillMaxSize()
        ) {
            Column {
                AlbumTopAppBar(
                    album = album,
                    padding = contentPadding.consume(bottom = Dp.Infinity)
                )

                AlbumContent(
                    album = album,
                    songs = songs,
                    contentPadding = contentPadding.consume(top = Dp.Infinity)
                )
            }
        }
    }
}

@Composable
private fun AlbumTopAppBar(
    album: Album,
    modifier: Modifier = Modifier,
    padding: PaddingValues = PaddingValues(0.dp)
) {
    val navigator = LocalAppNavigator.current
    LibraryAppBar(
        modifier = modifier,
        padding = padding,
        title = {
            Text(
                text = album.name,
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
