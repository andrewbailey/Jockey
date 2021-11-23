package dev.andrewbailey.music.ui.library.artists

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.andrewbailey.music.R
import dev.andrewbailey.music.ui.data.LocalMediaLibrary
import dev.andrewbailey.music.ui.library.common.ArtistList
import dev.andrewbailey.music.ui.navigation.ArtistScreen
import dev.andrewbailey.music.ui.navigation.LocalAppNavigator

@Composable
fun AllArtistsRoot(
    modifier: Modifier = Modifier
) {
    val mediaLibrary = LocalMediaLibrary.current
    val navigator = LocalAppNavigator.current

    val artists = mediaLibrary.artists.collectAsState().value
    when {
        artists == null -> AllArtistsLoadingState(modifier)
        artists.isEmpty() -> AllArtistsEmptyState(modifier)
        else -> ArtistList(
            artists = artists,
            modifier = modifier,
            onClickArtist = { _, artist ->
                navigator.push(ArtistScreen(artist))
            }
        )
    }
}

@Composable
private fun AllArtistsLoadingState(
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
private fun AllArtistsEmptyState(
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
