package dev.andrewbailey.music.ui.library.songs

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
import androidx.compose.ui.viewinterop.viewModel
import dev.andrewbailey.music.R
import dev.andrewbailey.music.ui.library.LibraryViewModel
import dev.andrewbailey.music.ui.library.common.SongList
import dev.andrewbailey.music.ui.root.PlaybackViewModel
import dev.andrewbailey.music.util.observe

@Composable
fun AllSongsRoot(
    modifier: Modifier = Modifier
) {
    val libraryViewModel = viewModel<LibraryViewModel>()
    val playbackViewModel = viewModel<PlaybackViewModel>()

    val songs = observe(libraryViewModel.songs)
    when {
        songs == null -> AllSongsLoadingState(modifier)
        songs.isEmpty() -> AllSongsEmptyState(modifier)
        else -> SongList(
            songs = songs,
            modifier = modifier,
            onClickSong = { index, _ ->
                playbackViewModel.playFrom(songs, index)
            }
        )
    }
}

@Composable
private fun AllSongsLoadingState(
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
private fun AllSongsEmptyState(
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
