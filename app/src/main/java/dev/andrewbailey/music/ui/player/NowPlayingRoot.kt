package dev.andrewbailey.music.ui.player

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.LazyColumnForIndexed
import androidx.compose.material.*
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawShadow
import androidx.compose.ui.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.LinearGradient
import androidx.compose.ui.graphics.asImageAsset
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
fun NowPlayingRoot() {
    val viewModel = viewModel<PlaybackViewModel>()
    val playbackState = observe(viewModel.playbackState)
    val navigator = AppNavigator.current

    ConstraintLayout(
        modifier = Modifier.fillMaxHeight()
    ) {
        val (toolbar, artwork, controls, queue) = createRefs()

        TopAppBar(
            modifier = Modifier
                .zIndex(1f)
                .constrainAs(toolbar) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                },
            backgroundColor = Color.Transparent,
            title = { Text(stringResource(R.string.page_title_now_playing)) },
            elevation = 0.dp,
            contentColor = Color.White,
            navigationIcon = {
                IconButton(
                    onClick = { navigator.navigateUp() }
                ) {
                    Icon(
                        asset = vectorResource(id = R.drawable.ic_close_24)
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
                        onClick = { viewModel.setShuffleMode(toggledShuffleMode) }
                    ) {
                        Icon(
                            asset = vectorResource(id = R.drawable.ic_shuffle),
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

        Box(
            modifier = Modifier
                .background(Color.Black)
                .constrainAs(artwork) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    width = Dimension.fillToConstraints
                    height = Dimension.wrapContent
                }
                .aspectRatio(1.0f)
                .scrim()
        ) {
            (playbackState as? MediaPlayerState.Prepared)?.artwork?.let { albumArt ->
                Image(
                    asset = albumArt.asImageAsset(),
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        NowPlayingControls(
            playbackState = playbackState,
            modifier = Modifier
                .drawShadow(elevation = 4.dp)
                .zIndex(4f)
                .background(color = MaterialTheme.colors.surface)
                .padding(16.dp)
                .constrainAs(controls) {
                    top.linkTo(artwork.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    width = Dimension.fillToConstraints
                }
        )

        NowPlayingQueue(
            queue = (playbackState?.transportState as? TransportState.Active)
                ?.queue?.queue.orEmpty(),
            modifier = Modifier.constrainAs(queue) {
                top.linkTo(controls.bottom)
                bottom.linkTo(parent.bottom)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                height = Dimension.fillToConstraints
            }
        )
    }
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

    ConstraintLayout(
        modifier = modifier
    ) {
        val (songName, artistName, albumName) = createRefs()
        val (seekBar, skipPrevious, playPause, skipNext) = createRefs()

        createHorizontalChain(
            skipPrevious,
            playPause,
            skipNext,
            chainStyle = ChainStyle.Spread
        )

        createVerticalChain(
            songName,
            artistName,
            albumName,
            seekBar,
            playPause,
            chainStyle = ChainStyle.Packed
        )

        val userSeekPosition = remember { mutableStateOf<Float?>(null) }

        val nowPlaying = playbackState.transportState.queue.nowPlaying.mediaItem
        Text(
            text = nowPlaying.name,
            style = MaterialTheme.typography.body1,
            color = MaterialTheme.colors.onBackground,
            modifier = Modifier.constrainAs(songName) {
                top.linkTo(parent.top)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                width = Dimension.fillToConstraints
            }
        )

        Text(
            text = nowPlaying.album?.name ?: "No album",
            style = MaterialTheme.typography.body2,
            color = MaterialTheme.colors.onBackground,
            modifier = Modifier.constrainAs(albumName) {
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                width = Dimension.fillToConstraints
            }
        )

        Text(
            text = nowPlaying.artist?.name ?: "No artist",
            style = MaterialTheme.typography.body2,
            color = MaterialTheme.colors.onBackground,
            modifier = Modifier.constrainAs(artistName) {
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                width = Dimension.fillToConstraints
            }
        )

        Slider(
            valueRange = 0f..(playbackState.durationMs?.toFloat() ?: 0f),
            value = userSeekPosition.value
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
                .constrainAs(seekBar) {
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
                .offset(y = 4.dp)
                .preferredHeight(36.dp)
        )

        IconButton(
            onClick = viewModel::skipPrevious,
            modifier = Modifier.constrainAs(skipPrevious) {
                top.linkTo(playPause.top)
                bottom.linkTo(playPause.bottom)
            }
        ) {
            Icon(
                asset = vectorResource(id = R.drawable.ic_skip_previous),
                tint = MaterialTheme.colors.onBackground
            )
        }

        IconButton(
            onClick = {
                if (playbackState.transportState.status == PlaybackState.PLAYING) {
                    viewModel.pause()
                } else {
                    viewModel.play()
                }
            },
            modifier = Modifier.constrainAs(playPause) {
                bottom.linkTo(parent.bottom)
            }
        ) {
            if (playbackState.transportState.status == PlaybackState.PLAYING) {
                Icon(
                    asset = vectorResource(id = R.drawable.ic_pause),
                    tint = MaterialTheme.colors.onBackground
                )
            } else {
                Icon(
                    asset = vectorResource(id = R.drawable.ic_play),
                    tint = MaterialTheme.colors.onBackground
                )
            }
        }

        IconButton(
            onClick = viewModel::skipNext,
            modifier = Modifier.constrainAs(skipNext) {
                top.linkTo(playPause.top)
                bottom.linkTo(playPause.bottom)
            }
        ) {
            Icon(
                asset = vectorResource(id = R.drawable.ic_skip_next),
                tint = MaterialTheme.colors.onBackground
            )
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
