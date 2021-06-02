package dev.andrewbailey.music.ui.library.albums

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.coil.rememberCoilPainter
import com.google.accompanist.insets.ExperimentalAnimatedInsets
import dev.andrewbailey.music.R
import dev.andrewbailey.music.model.Album
import dev.andrewbailey.music.model.Song
import dev.andrewbailey.music.ui.layout.LibraryPageLayout
import dev.andrewbailey.music.ui.layout.StatusBarBackground
import dev.andrewbailey.music.ui.library.LibraryViewModel
import dev.andrewbailey.music.ui.library.common.songs
import dev.andrewbailey.music.ui.navigation.LocalAppNavigator
import dev.andrewbailey.music.ui.root.PlaybackViewModel
import dev.andrewbailey.music.util.pluralsResource

@OptIn(ExperimentalAnimatedInsets::class)
@Composable
fun AlbumPage(
    album: Album,
    modifier: Modifier = Modifier
) {
    val libraryViewModel = viewModel<LibraryViewModel>()
    val playbackViewModel = viewModel<PlaybackViewModel>()
    val songs = libraryViewModel.getSongsInAlbum(album).observeAsState().value

    LibraryPageLayout(
        modifier = modifier
    ) {
        Surface(
            modifier = Modifier.fillMaxSize()
        ) {
            Column {
                StatusBarBackground()
                AlbumTopAppBar(album)

                LazyColumn {
                    item {
                        AlbumHeader(
                            album = album,
                            songs = songs
                        )
                    }

                    if (songs == null) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth()) {
                                CircularProgressIndicator(
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                        }
                    } else {
                        songs(
                            songs = songs,
                            onClickSong = { index, _ ->
                                playbackViewModel.playFrom(songs, index)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AlbumTopAppBar(
    album: Album,
    modifier: Modifier = Modifier
) {
    val navigator = LocalAppNavigator.current
    TopAppBar(
        modifier = modifier,
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

@Composable
private fun AlbumHeader(
    album: Album,
    modifier: Modifier = Modifier,
    songs: List<Song>? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .padding(16.dp)
    ) {
        Image(
            painter = rememberCoilPainter(album),
            contentDescription = stringResource(id = R.string.content_description_album_art),
            modifier = Modifier
                .align(Alignment.Top)
                .size(128.dp)
                .clip(MaterialTheme.shapes.medium)
        )

        Column(
            modifier = Modifier
                .padding(start = 16.dp)
        ) {
            Text(
                text = album.name,
                style = MaterialTheme.typography.h5,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = album.artist?.name.orEmpty(),
                style = MaterialTheme.typography.subtitle1,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = listOfNotNull(
                    songs?.mapNotNull { it.toMediaMetadata().year }?.maxOrNull(),
                    songs?.size?.let { pluralsResource(R.plurals.song_count, it, it) },
                    songs?.sumOf { it.toMediaMetadata().durationMs ?: 0 }?.let { albumDurationMs ->
                        val seconds = (albumDurationMs / 1000) % 60
                        val minutes = (albumDurationMs / 60_000) % 60
                        val hours = albumDurationMs / 3_600_000

                        when {
                            hours > 0 -> stringResource(R.string.duration_hr_min, hours, minutes)
                            else -> stringResource(R.string.duration_min_sec, minutes, seconds)
                        }
                    }
                ).joinToString(separator = " \u2022 "),
                style = MaterialTheme.typography.body2,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (songs?.isNotEmpty() == true) {
                val playbackViewModel = viewModel<PlaybackViewModel>()

                OutlinedButton(
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colors.secondary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    onClick = {
                        playbackViewModel.playShuffled(songs)
                    }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_shuffle),
                        contentDescription = null
                    )
                    Text(
                        text = stringResource(id = R.string.action_shuffle_all),
                        maxLines = 1,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    }
}
