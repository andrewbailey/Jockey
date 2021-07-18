package dev.andrewbailey.music.ui.library.albums

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.andrewbailey.music.R
import dev.andrewbailey.music.ui.data.LocalMediaLibrary
import dev.andrewbailey.music.ui.library.common.AlbumList
import dev.andrewbailey.music.ui.navigation.AlbumScreen
import dev.andrewbailey.music.ui.navigation.LocalAppNavigator

@Composable
fun AllAlbumsRoot(
    modifier: Modifier = Modifier
) {
    val mediaLibrary = LocalMediaLibrary.current
    val navigator = LocalAppNavigator.current

    val albums = mediaLibrary.albums.collectAsState().value
    when {
        albums == null -> AllAlbumsLoadingState(modifier)
        albums.isNullOrEmpty() -> AllAlbumsEmptyState(modifier)
        else -> AlbumList(
            albums = albums.orEmpty(),
            modifier = modifier,
            onClickAlbum = { _, album ->
                navigator.push(AlbumScreen(album))
            }
        )
    }
}

@Composable
private fun AllAlbumsLoadingState(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun AllAlbumsEmptyState(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            style = MaterialTheme.typography.subtitle1,
            textAlign = TextAlign.Center,
            text = stringResource(id = R.string.library_songs_empty_header),
            modifier = Modifier.widthIn(max = 280.dp)
        )

        Text(
            style = MaterialTheme.typography.body2,
            textAlign = TextAlign.Center,
            text = stringResource(id = R.string.library_songs_empty_description),
            modifier = Modifier.widthIn(max = 280.dp)
        )
    }
}
