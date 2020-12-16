package dev.andrewbailey.music.ui.player

import androidx.annotation.FloatRange
import androidx.compose.foundation.Image
import androidx.compose.foundation.InteractionState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ConstraintLayout
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumnForIndexed
import androidx.compose.material.AmbientContentColor
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.ListItem
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.LinearGradient
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.viewModel
import androidx.compose.ui.zIndex
import dev.andrewbailey.encore.model.QueueItem
import dev.andrewbailey.encore.player.state.MediaPlayerState
import dev.andrewbailey.encore.player.state.PlaybackState
import dev.andrewbailey.encore.player.state.ShuffleMode
import dev.andrewbailey.encore.player.state.TransportState
import dev.andrewbailey.encore.provider.mediastore.LocalSong
import dev.andrewbailey.music.R
import dev.andrewbailey.music.ui.navigation.AppNavigator
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
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
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
    playbackState: MediaPlayerState<LocalSong>?,
    modifier: Modifier = Modifier
) {
    val navigator = AppNavigator.current

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
                    imageVector = vectorResource(id = R.drawable.ic_close_24)
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
                        imageVector = vectorResource(id = R.drawable.ic_shuffle),
                        tint = AmbientContentColor.current.copy(
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
    playbackState: MediaPlayerState<LocalSong>?,
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
    playbackState: MediaPlayerState.Prepared<LocalSong>,
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

        val sliderInteractionState = remember { InteractionState() }
        Slider(
            valueRange = 0f..(playbackState.durationMs?.toFloat() ?: 0f),
            interactionState = sliderInteractionState,
            value = userSeekPosition.value.takeIf { sliderInteractionState.value.isNotEmpty() }
                ?: playbackState.transportState.seekPosition.seekPositionMillis.toFloat(),
            onValueChange = { userSeekPosition.value = it },
            onValueChangeEnd = {
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
                        imageVector = vectorResource(id = R.drawable.ic_skip_previous),
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
                            imageVector = vectorResource(id = R.drawable.ic_pause),
                            tint = MaterialTheme.colors.onBackground
                        )
                    }
                } else {
                    IconButton(onClick = viewModel::play) {
                        Icon(
                            imageVector = vectorResource(id = R.drawable.ic_play),
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
                        imageVector = vectorResource(id = R.drawable.ic_skip_next),
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
    ConstraintLayout(
        modifier = modifier
    ) {
        Text(
            text = "Nothing is playing"
        )
    }
}

@Composable
private fun NowPlayingQueue(
    queue: List<QueueItem<LocalSong>>,
    modifier: Modifier = Modifier
) {
    val viewModel = viewModel<PlaybackViewModel>()

    Surface(
        elevation = 0.dp,
        color = MaterialTheme.colors.background,
        modifier = modifier
    ) {
        LazyColumnForIndexed(
            items = queue
        ) { index, queueItem ->
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
    }
}

private fun formattedAlbumArtist(item: LocalSong): String =
    listOfNotNull(item.album?.name, item.artist?.name).joinToString(" - ")

private fun Modifier.scrim() = drawWithContent {
    drawContent()
    drawRect(
        brush = LinearGradient(
            0.0f to Color.Black.copy(alpha = 0.40f),
            0.5f to Color.Black.copy(alpha = 0.05f),
            1.0f to Color.Black.copy(alpha = 0.00f),
            startX = 0f,
            startY = 0f,
            endX = 0f,
            endY = size.height
        )
    )
}
