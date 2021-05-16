package dev.andrewbailey.music.ui.library.artists

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.andrewbailey.music.R
import dev.andrewbailey.music.ui.library.LibraryViewModel
import dev.andrewbailey.music.ui.library.common.ArtistList
import dev.andrewbailey.music.util.observe

@Composable
fun AllArtistsRoot(
    modifier: Modifier = Modifier
) {
    val libraryViewModel = viewModel<LibraryViewModel>()

    val artists = observe(libraryViewModel.artists)
    when {
        artists == null -> AllArtistsLoadingState(modifier)
        artists.isEmpty() -> AllArtistsEmptyState(modifier)
        else -> ArtistList(
            artists = artists,
            modifier = modifier
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
