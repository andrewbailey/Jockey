package dev.andrewbailey.music.ui.player

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.andrewbailey.encore.player.state.QueueState
import dev.andrewbailey.music.R
import dev.andrewbailey.music.model.Album
import dev.andrewbailey.music.model.Artist
import dev.andrewbailey.music.model.Song

@Composable
fun NowPlayingBottomSheet(
    queue: QueueState<Song>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        NowPlayingToolbar()
        NowPlayingQueue(queue = queue)
    }
}

@Composable
private fun NowPlayingToolbar(
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = { Text(stringResource(R.string.page_title_queue)) },
        backgroundColor = MaterialTheme.colors.background,
        elevation = 0.dp,
        modifier = modifier
    )
}

@Composable
private fun NowPlayingQueue(
    queue: QueueState<Song>,
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    LazyColumn(
        modifier = modifier,
        state = state,
        contentPadding = contentPadding
    ) {
        items(queue.queue) { item ->
            NowPlayingQueueItem(
                song = item.mediaItem,
                isPlaying = item == queue.nowPlaying
            )
        }
    }
}

@Composable
@OptIn(ExperimentalMaterialApi::class)
private fun NowPlayingQueueItem(
    song: Song,
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    onClickSong: ((Song) -> Unit)? = null
) {
    Column(modifier) {
        ListItem(
            icon = {

            },
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
            modifier = if (onClickSong != null) {
                Modifier.clickable(onClick = { onClickSong(song) })
            } else {
                Modifier
            }
        )
        Divider()
    }
}

private fun formattedAlbumArtist(album: Album?, artist: Artist?): String =
    listOfNotNull(album?.name, artist?.name).joinToString(" - ")
