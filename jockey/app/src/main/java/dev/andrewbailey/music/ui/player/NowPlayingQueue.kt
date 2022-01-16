package dev.andrewbailey.music.ui.player

import androidx.annotation.FloatRange
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.AppBarDefaults
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.ListItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.rememberInsetsPaddingValues
import dev.andrewbailey.encore.player.state.QueueState
import dev.andrewbailey.music.R
import dev.andrewbailey.music.model.Song
import dev.andrewbailey.music.ui.data.LocalPlaybackController
import dev.andrewbailey.music.util.heightOf

@Composable
fun NowPlayingQueue(
    queue: QueueState<Song>,
    modifier: Modifier = Modifier,
    expandQueue: () -> Unit = {},
    collapseQueue: () -> Unit = {},
    @FloatRange(from = 0.0, to = 1.0)
    percentExpanded: Float = 1f
) {
    Column(
        modifier = modifier
    ) {
        NowPlayingToolbar(
            expandQueue = expandQueue,
            collapseQueue = collapseQueue,
            percentExpanded = percentExpanded,
            elevation = AppBarDefaults.TopAppBarElevation *
                (percentExpanded * 2).coerceIn(0f..1f)
        )

        val nextPlayingIndex = (queue.queueIndex + 1).coerceAtMost(queue.queue.size - 1)
        val scrollState = rememberLazyListState(
            initialFirstVisibleItemIndex = nextPlayingIndex
        )

        NowPlayingQueueItems(
            queue = queue,
            scrollable = percentExpanded == 1f,
            selectable = percentExpanded == 1f,
            state = scrollState,
            contentPadding = rememberInsetsPaddingValues(
                insets = LocalWindowInsets.current.navigationBars
            )
        )
    }
}

@Composable
fun nowPlayingQueueCollapsedHeightPx(): Int {
    return heightOf {
        Column {
            NowPlayingToolbar(
                expandQueue = { },
                collapseQueue = { }
            )
            NowPlayingQueueItem(
                songName = "",
                albumName = "",
                artistName = "",
                isPlaying = false
            )
        }
    }
}

@Composable
private fun NowPlayingToolbar(
    expandQueue: () -> Unit,
    collapseQueue: () -> Unit,
    modifier: Modifier = Modifier,
    @FloatRange(from = 0.0, to = 1.0)
    percentExpanded: Float = 1f,
    elevation: Dp = AppBarDefaults.TopAppBarElevation
) {
    TopAppBar(
        title = { Text(stringResource(R.string.page_title_queue)) },
        navigationIcon = {
            IconButton(
                onClick = {
                    if (percentExpanded > 0) {
                        collapseQueue()
                    } else {
                        expandQueue()
                    }
                }
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_chevron_up),
                    contentDescription = "", // TODO
                    modifier = Modifier.rotate(-180 * percentExpanded)
                )
            }
        },
        backgroundColor = MaterialTheme.colors.background,
        elevation = elevation,
        modifier = modifier
    )
}

@Composable
private fun NowPlayingQueueItems(
    queue: QueueState<Song>,
    modifier: Modifier = Modifier,
    scrollable: Boolean = true,
    selectable: Boolean = true,
    state: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    val playbackController = LocalPlaybackController.current

    LazyColumn(
        modifier = modifier,
        userScrollEnabled = scrollable,
        state = state,
        contentPadding = contentPadding
    ) {
        itemsIndexed(queue.queue) { index, item ->
            NowPlayingQueueItem(
                songName = item.mediaItem.name,
                albumName = item.mediaItem.album?.name,
                artistName = item.mediaItem.artist?.name,
                isPlaying = item == queue.nowPlaying,
                onClick = {
                    playbackController.playAtQueueIndex(index)
                }.takeIf { selectable }
            )
            Divider()
        }
    }
}

@Composable
@OptIn(ExperimentalMaterialApi::class)
private fun NowPlayingQueueItem(
    songName: String,
    albumName: String?,
    artistName: String?,
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    ListItem(
        text = {
            Text(
                text = songName,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        secondaryText = {
            Text(
                text = listOfNotNull(albumName, artistName).joinToString(" - "),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        trailing = {
            if (isPlaying) {
                Icon(
                    painter = painterResource(R.drawable.ic_playing),
                    contentDescription = null,
                    tint = MaterialTheme.colors.secondary,
                    modifier = Modifier
                        .focusable(false)
                        .size(18.dp)
                )
            }
        },
        modifier = if (onClick != null) {
            modifier.clickable(onClick = { onClick() })
        } else {
            modifier
        }
    )
}
