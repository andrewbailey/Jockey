package dev.andrewbailey.music.ui.library

import androidx.compose.foundation.Text
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumnForIndexed
import androidx.compose.material.Divider
import androidx.compose.material.ListItem
import androidx.compose.material.Surface
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.viewModel
import dev.andrewbailey.encore.provider.mediastore.LocalAlbum
import dev.andrewbailey.encore.provider.mediastore.LocalArtist
import dev.andrewbailey.music.R
import dev.andrewbailey.music.ui.navigation.AppNavigator
import dev.andrewbailey.music.ui.navigation.LibraryPage
import dev.andrewbailey.music.ui.navigation.NowPlayingScreen
import dev.andrewbailey.music.ui.root.PlaybackViewModel
import dev.andrewbailey.music.util.observe

@Composable
fun LibraryRoot(
    page: LibraryPage
) {
    val navigator = AppNavigator.current

    Column {
        TopAppBar(
            title = { Text(stringResource(R.string.app_name)) }
        )

        Surface(Modifier.weight(1f)) {
            when (page) {
                LibraryPage.Songs -> SongList()
            }
        }

        Box {
            CollapsedPlayerControls(
                modifier = Modifier.clickable(onClick = {
                    navigator.push(NowPlayingScreen)
                })
            )
        }
    }
}

@Composable
fun SongList(
    modifier: Modifier = Modifier
) {
    val viewModel = viewModel<LibraryViewModel>()
    val playbackViewModel = viewModel<PlaybackViewModel>()

    val songs = observe(viewModel.songs).orEmpty()
    LazyColumnForIndexed(
        items = songs,
        modifier = modifier
    ) { index, song ->
        ListItem(
            text = {
                Text(
                    text = song.name,
                    maxLines = 1
                )
            },
            secondaryText = {
                Text(
                    text = formattedAlbumArtist(song.album, song.artist),
                    maxLines = 1
                )
            },
            modifier = Modifier
                .clickable(onClick = {
                    playbackViewModel.playFrom(songs, index)
                })
        )
        Divider()
    }
}

private fun formattedAlbumArtist(album: LocalAlbum?, artist: LocalArtist?): String =
    listOfNotNull(album?.name, artist?.name).joinToString(" - ")
