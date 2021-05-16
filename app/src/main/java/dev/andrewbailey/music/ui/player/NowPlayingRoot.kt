package dev.andrewbailey.music.ui.player

import androidx.annotation.FloatRange
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.ListItem
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Slider
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.andrewbailey.encore.model.QueueItem
import dev.andrewbailey.encore.player.state.MediaPlayerState
import dev.andrewbailey.encore.player.state.PlaybackState
import dev.andrewbailey.encore.player.state.ShuffleMode
import dev.andrewbailey.encore.player.state.TransportState
import dev.andrewbailey.music.R
import dev.andrewbailey.music.model.Song
import dev.andrewbailey.music.ui.navigation.LocalAppNavigator
import dev.andrewbailey.music.ui.root.PlaybackViewModel
import dev.andrewbailey.music.util.observe

@Composable
fun NowPlayingRoot(
    modifier: Modifier = Modifier,
    @FloatRange(from = 0.0, to = 1.0)
    percentVisible: Float = 1.0f
) {
    val viewModel = viewModel<PlaybackViewModel>()
    val playbackState = observe(viewModel.playbackState)

    Column(
        modifier = modifier.fillMaxHeight()
    ) {
        Box(
            modifier = Modifier
                .background(Color.Black)
                .fillMaxWidth()
                .aspectRatio(1.0f)
        ) {
            NowPlayingToolbar(
                playbackViewModel = viewModel,
                playbackState = playbackState,
                modifier = Modifier.alpha((2 * percentVisible - 1).coerceIn(0f..1f))
            )

            (playbackState as? MediaPlayerState.Prepared)?.artwork?.let { albumArt ->
                Image(
                    bitmap = albumArt.asImageBitmap(),
                    contentDescription = stringResource(R.string.content_description_album_art),
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .scrim()
                )
            }
        }

        NowPlayingControls(
            playbackState = playbackState,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .shadow(elevation = 4.dp)
                .zIndex(4f)
                .background(color = MaterialTheme.colors.surface)
                .padding(16.dp)
        )

        NowPlayingQueue(
            queue = (playbackState?.transportState as? TransportState.Active)
                ?.queue?.queue.orEmpty(),
            modifier = Modifier.fillMaxHeight()
        )
    }
}

@Composable
private fun NowPlayingToolbar(
    playbackViewModel: PlaybackViewModel,
    playbackState: MediaPlayerState<Song>?,
    modifier: Modifier = Modifier
) {
    val navigator = LocalAppNavigator.current

    TopAppBar(
        modifier = modifier
            .zIndex(1f),
        backgroundColor = Color.Transparent,
        title = { Text(stringResource(R.string.page_title_now_playing)) },
        elevation = 0.dp,
        contentColor = Color.White,
        navigationIcon = {
            IconButton(
                onClick = { navigator.pop() }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_close_24),
                    contentDescription = stringResource(R.string.content_description_close_page)
                )
            }
        },
        actions = {
            playbackState?.transportState?.shuffleMode?.let { shuffleMode ->
                val toggledShuffleMode = when (shuffleMode) {
                    ShuffleMode.LINEAR -> ShuffleMode.SHUFFLED
                    ShuffleMode.SHUFFLED -> ShuffleMode.LINEAR
                }

                IconButton(
                    onClick = { playbackViewModel.setShuffleMode(toggledShuffleMode) }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_shuffle),
                        contentDescription = stringResource(
                            id = when (shuffleMode) {
                                ShuffleMode.LINEAR -> R.string.content_description_disable_shuffle
                                ShuffleMode.SHUFFLED -> R.string.content_description_enable_shuffle
                            }
                        ),
                        tint = LocalContentColor.current.copy(
                            alpha = when (shuffleMode) {
                                ShuffleMode.LINEAR -> 0.5f
                                ShuffleMode.SHUFFLED -> 1.0f
                            }
                        )
                    )
                }
            }

        }
    )
}

@Composable
private fun NowPlayingControls(
    playbackState: MediaPlayerState<Song>?,
    modifier: Modifier = Modifier
) {
    if (playbackState is MediaPlayerState.Prepared) {
        ActiveNowPlayingControls(
            playbackState = playbackState,
            modifier = modifier
        )
    } else {
        InactiveNowPlayingControls(
            modifier = modifier
        )
    }
}

@Composable
private fun ActiveNowPlayingControls(
    playbackState: MediaPlayerState.Prepared<Song>,
    modifier: Modifier
) {
    val viewModel = viewModel<PlaybackViewModel>()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        val userSeekPosition = remember { mutableStateOf<Float?>(null) }
        val nowPlaying = playbackState.transportState.queue.nowPlaying.mediaItem

        Text(
            text = nowPlaying.name,
            style = MaterialTheme.typography.body1,
            color = MaterialTheme.colors.onBackground,
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = nowPlaying.album?.name ?: "No album",
            style = MaterialTheme.typography.body2,
            color = MaterialTheme.colors.onBackground,
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = nowPlaying.artist?.name ?: "No artist",
            style = MaterialTheme.typography.body2,
            color = MaterialTheme.colors.onBackground,
            modifier = Modifier.fillMaxWidth()
        )

        val sliderInteractionState = remember { MutableInteractionSource() }
        Slider(
            valueRange = 0f..(playbackState.durationMs?.toFloat() ?: 0f),
            interactionSource = sliderInteractionState,
            value = userSeekPosition.value
                .takeIf { sliderInteractionState.collectIsDraggedAsState().value }
                ?: playbackState.transportState.seekPosition.seekPositionMillis.toFloat(),
            onValueChange = { userSeekPosition.value = it },
            onValueChangeFinished = {
                viewModel.seekTo(
                    checkNotNull(userSeekPosition.value) {
                        "Failed to finalize seek because the requested seek position is unknown."
                    }.toLong()
                )
                userSeekPosition.value = null
            },
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = 4.dp)
                .height(36.dp)
        )

        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth(0.85f)
        ) {
            Box(
                modifier = Modifier.weight(1.0f),
                contentAlignment = Alignment.Center
            ) {
                IconButton(onClick = viewModel::skipPrevious) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_skip_previous),
                        contentDescription = stringResource(
                            id = R.string.content_description_skip_previous
                        ),
                        tint = MaterialTheme.colors.onBackground
                    )
                }
            }

            Box(
                modifier = Modifier.weight(1.0f),
                contentAlignment = Alignment.Center
            ) {
                if (playbackState.transportState.status == PlaybackState.PLAYING) {
                    IconButton(onClick = viewModel::pause) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_pause),
                            contentDescription = stringResource(R.string.content_description_pause),
                            tint = MaterialTheme.colors.onBackground
                        )
                    }
                } else {
                    IconButton(onClick = viewModel::play) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_play),
                            contentDescription = stringResource(R.string.content_description_play),
                            tint = MaterialTheme.colors.onBackground
                        )
                    }
                }
            }

            Box(
                modifier = Modifier.weight(1.0f),
                contentAlignment = Alignment.Center
            ) {
                IconButton(onClick = viewModel::skipNext) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_skip_next),
                        contentDescription = stringResource(R.string.content_description_skip_next),
                        tint = MaterialTheme.colors.onBackground
                    )
                }
            }
        }
    }
}

@Composable
private fun InactiveNowPlayingControls(
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
    ) {
        Text(
            text = "Nothing is playing"
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun NowPlayingQueue(
    queue: List<QueueItem<Song>>,
    modifier: Modifier = Modifier
) {
    val viewModel = viewModel<PlaybackViewModel>()

    Surface(
        elevation = 0.dp,
        color = MaterialTheme.colors.background,
        modifier = modifier
    ) {
        LazyColumn {
            itemsIndexed(
                items = queue,
                itemContent = { index, queueItem ->
                    ListItem(
                        text = {
                            Text(queueItem.mediaItem.name)
                        },
                        secondaryText = {
                            Text(formattedAlbumArtist(queueItem.mediaItem))
                        },
                        modifier = Modifier
                            .clickable(
                                onClick = {
                                    viewModel.playAtQueueIndex(index)
                                }
                            )
                    )
                    Divider()
                }
            )
        }
    }
}

private fun formattedAlbumArtist(item: Song): String =
    listOfNotNull(item.album?.name, item.artist?.name).joinToString(" - ")

private fun Modifier.scrim() = drawWithContent {
    drawContent()
    drawRect(
        brush = Brush.linearGradient(
            colorStops = arrayOf(
                0.0f to Color.Black.copy(alpha = 0.40f),
                0.5f to Color.Black.copy(alpha = 0.05f),
                1.0f to Color.Black.copy(alpha = 0.00f)
            ),
            start = Offset(0f, 0f),
            end = Offset(0f, size.height)
        )
    )
}
