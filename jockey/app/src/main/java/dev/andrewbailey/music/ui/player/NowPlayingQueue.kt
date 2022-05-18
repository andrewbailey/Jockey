package dev.andrewbailey.music.ui.player

import androidx.annotation.FloatRange
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.andrewbailey.encore.player.state.QueueState
import dev.andrewbailey.music.R
import dev.andrewbailey.music.model.Song
import dev.andrewbailey.music.ui.data.LocalPlaybackController
import dev.andrewbailey.music.ui.layout.ModalState
import dev.andrewbailey.music.ui.layout.ModalStateValue.Collapsed
import dev.andrewbailey.music.ui.layout.ModalStateValue.Expanded
import dev.andrewbailey.music.util.heightOf
import dev.andrewbailey.music.util.plus

@Composable
fun CollapsibleNowPlayingQueue(
    queue: QueueState<Song>,
    modalState: ModalState,
    modifier: Modifier = Modifier,
    expandQueue: () -> Unit = {},
    collapseQueue: () -> Unit = {},
) {
    Column(
        modifier = modifier
    ) {
        NowPlayingToolbar(
            expandQueue = expandQueue,
            collapseQueue = collapseQueue,
            percentExpanded = modalState.percentExpanded,
            elevation = AppBarDefaults.TopAppBarElevation *
                (modalState.percentExpanded * 2).coerceIn(0f..1f)
        )

        val nextPlayingIndex = (queue.queueIndex + 1).coerceAtMost(queue.queue.size - 1)
        val scrollState = rememberLazyListState(
            initialFirstVisibleItemIndex = nextPlayingIndex
        )

        // The effective state is the state the modal has settled in. This accounts primarily for
        // delays that happen when the modal is collapsing.
        var effectiveModalState by remember { mutableStateOf(modalState.targetValue) }

        // When the sheet collapses, reset the scroll position of the list
        LaunchedEffect(modalState.confirmedState) {
            if (modalState.confirmedState == Collapsed) {
                scrollState.animateScrollToItem(nextPlayingIndex)
                effectiveModalState = modalState.currentValue
            }
        }

        // Keep the scroll state in sync with the next track while the modal is collapsed
        if (effectiveModalState == Collapsed) {
            LaunchedEffect(nextPlayingIndex) {
                scrollState.scrollToItem(nextPlayingIndex)
            }
        }

        // Show the full list of songs as soon as the modal becomes partially expanded
        if (modalState.currentValue == Collapsed) {
            effectiveModalState = if (modalState.percentExpanded > 0) {
                Expanded
            } else {
                Collapsed
            }
        }

        Crossfade(targetState = effectiveModalState == Expanded) { showFullList ->
            if (showFullList) {
                NowPlayingQueueItems(
                    queue = queue,
                    scrollable = modalState.percentExpanded == 1f,
                    selectable = modalState.percentExpanded == 1f,
                    state = scrollState,
                    contentPadding = WindowInsets.navigationBars.asPaddingValues()
                )
            } else {
                Crossfade(targetState = queue.queue[nextPlayingIndex]) { song ->
                    NowPlayingQueueItem(
                        songName = song.mediaItem.name,
                        albumName = song.mediaItem.album?.name,
                        artistName = song.mediaItem.artist?.name,
                        isPlaying = false
                    )
                }
            }
        }
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

    BoxWithConstraints(
        modifier = modifier
    ) {
        // Add enough padding to the bottom of the LazyColumn to ensure that it's always possible
        // for the next track's cell to be shown at the top of the view.
        val heightOfQueuedItems = with(LocalDensity.current) {
            val listItemHeightPx = heightOf {
                NowPlayingQueueItem(
                    songName = "",
                    albumName = "",
                    artistName = "",
                    isPlaying = false
                )
            }
            val numberOfVisibleItemsAtBottomOfList = (queue.queue.size - (queue.queueIndex + 1))
                .coerceAtLeast(1)

            (numberOfVisibleItemsAtBottomOfList * listItemHeightPx).toDp()
        }

        val bottomPadding = contentPadding.calculateBottomPadding()
        val additionalBottomPadding = (maxHeight - heightOfQueuedItems - bottomPadding)
            .coerceAtLeast(0.dp)

        LazyColumn(
            userScrollEnabled = scrollable,
            state = state,
            contentPadding = contentPadding + PaddingValues(bottom = additionalBottomPadding)
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
