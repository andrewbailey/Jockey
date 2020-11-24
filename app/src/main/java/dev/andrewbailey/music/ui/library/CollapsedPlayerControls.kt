package dev.andrewbailey.music.ui.library

import androidx.compose.animation.Crossfade
import androidx.compose.animation.DpPropKey
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FloatPropKey
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.transitionDefinition
import androidx.compose.animation.core.tween
import androidx.compose.animation.transition
import androidx.compose.foundation.Image
import androidx.compose.foundation.Text
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ChainStyle
import androidx.compose.foundation.layout.ConstraintLayout
import androidx.compose.foundation.layout.Dimension
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.preferredHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawOpacity
import androidx.compose.ui.graphics.asImageAsset
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.annotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.viewModel
import dev.andrewbailey.encore.player.state.MediaPlayerState
import dev.andrewbailey.encore.player.state.PlaybackState
import dev.andrewbailey.encore.provider.mediastore.LocalSong
import dev.andrewbailey.music.R
import dev.andrewbailey.music.ui.root.PlaybackViewModel
import dev.andrewbailey.music.util.observe

private val controlBarHeight = 50.dp

@Composable
fun CollapsedPlayerControls(
    modifier: Modifier = Modifier
) {
    val playbackViewModel = viewModel<PlaybackViewModel>()
    val playbackState = observe(playbackViewModel.playbackState)

    val contentOpacity = remember { FloatPropKey() }
    val contentHeight = remember { DpPropKey() }

    val visibilityTransition = remember {
        transitionDefinition<Boolean> {
            state(true) {
                this[contentOpacity] = 1f
                this[contentHeight] = controlBarHeight
            }

            state(false) {
                this[contentOpacity] = 0f
                this[contentHeight] = 0.dp
            }

            transition(false to true) {
                contentOpacity using tween(
                    easing = LinearOutSlowInEasing,
                    durationMillis = 175,
                    delayMillis = 75
                )

                contentHeight using tween(
                    easing = LinearOutSlowInEasing,
                    durationMillis = 250
                )
            }

            transition(true to false) {
                contentOpacity using tween(
                    easing = FastOutLinearInEasing,
                    durationMillis = 175
                )

                contentHeight using tween(
                    easing = FastOutLinearInEasing,
                    durationMillis = 250
                )
            }
        }
    }

    val previousContent = remember { mutableStateOf(playbackState as? MediaPlayerState.Prepared) }
    val previousVisibility = remember { mutableStateOf(playbackState is MediaPlayerState.Prepared) }
    val transitionState = transition(
        definition = visibilityTransition,
        initState = previousVisibility.value,
        toState = playbackState is MediaPlayerState.Prepared,
        onStateChangeFinished = { isVisible ->
            previousVisibility.value = isVisible
            if (!isVisible) {
                previousContent.value = null
            }
        }
    )

    Box(modifier = modifier.preferredHeight(transitionState[contentHeight])) {
        if (playbackState is MediaPlayerState.Prepared) {
            previousContent.value = playbackState
        }

        val playbackStateToDisplay = previousContent.value
        if (playbackStateToDisplay != null) {
            PopulatedCollapsedPlaybackControls(
                playbackState = playbackStateToDisplay,
                modifier = Modifier
                    .height(controlBarHeight)
                    .offset(y = (controlBarHeight - transitionState[contentHeight]) / 2)
                    .drawOpacity(transitionState[contentOpacity])
                    .fillMaxWidth(),
                onPlayClicked = playbackViewModel::play,
                onPauseClicked = playbackViewModel::pause,
                onSkipNextClicked = playbackViewModel::skipNext
            )
        }
    }
}

@Composable
private fun PopulatedCollapsedPlaybackControls(
    playbackState: MediaPlayerState.Prepared<LocalSong>,
    modifier: Modifier = Modifier,
    onPlayClicked: () -> Unit = {},
    onPauseClicked: () -> Unit = {},
    onSkipNextClicked: () -> Unit = {}
) {
    ConstraintLayout(
        modifier = modifier
    ) {
        val artwork = createRef()
        val mediaDescription = createRef()
        val seekBar = createRef()

        val (playPause, skipNext) = createRefs()

        createHorizontalChain(
            elements = arrayOf(playPause, skipNext),
            chainStyle = ChainStyle.Packed(bias = 1.0f)
        )

        Box(
            modifier = Modifier.constrainAs(artwork) {
                top.linkTo(parent.top, margin = 2.dp)
                bottom.linkTo(parent.bottom)
                start.linkTo(parent.start, margin = 4.dp)
                width = Dimension.value(40.dp)
                height = Dimension.value(40.dp)
            }.border(
                width = 1.dp,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.15f),
                shape = RoundedCornerShape(size = 4.dp)
            )
        ) {
            Crossfade(
                current = playbackState.artwork,
                animation = tween(easing = LinearEasing)
            ) { artwork ->
                if (artwork != null) {
                    Image(
                        asset = artwork.asImageAsset(),
                        modifier = Modifier.clip(RoundedCornerShape(size = 4.dp))
                    )
                }
            }
        }

        val nowPlaying = playbackState.transportState.queue.nowPlaying.mediaItem
        Text(
            text = annotatedString {
                append(nowPlaying.name)
                nowPlaying.artist?.name?.let { artistName ->
                    append("   ")
                    pushStyle(
                        SpanStyle(
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                        )
                    )
                    append(artistName)
                    pop()
                }
            },
            style = MaterialTheme.typography.body2,
            color = MaterialTheme.colors.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.constrainAs(mediaDescription) {
                linkTo(
                    top = parent.top,
                    bottom = parent.bottom,
                    start = artwork.end,
                    end = playPause.start,
                    startMargin = 8.dp,
                    endMargin = 8.dp,
                    horizontalBias = 0f
                )
                width = Dimension.fillToConstraints
            }
        )

        LinearProgressIndicator(
            progress = playbackState.durationMs?.let { duration ->
                val percent = playbackState.transportState.seekPosition
                    .seekPositionMillis.toFloat() / duration

                percent.coerceIn(0f..1f)
            } ?: 0f,
            color = MaterialTheme.colors.secondary,
            modifier = Modifier.constrainAs(seekBar) {
                top.linkTo(parent.top)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                width = Dimension.fillToConstraints
                height = Dimension.value(3.dp)
            }.clipToBounds()
        )

        val isPlaying = playbackState.transportState.status == PlaybackState.PLAYING
        IconButton(
            modifier = Modifier.constrainAs(playPause) {
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
            },
            onClick = if (isPlaying) {
                onPauseClicked
            } else {
                onPlayClicked
            }
        ) {
            Icon(
                asset = vectorResource(
                    id = if (isPlaying) {
                        R.drawable.ic_pause
                    } else {
                        R.drawable.ic_play
                    }
                )
            )
        }

        IconButton(
            modifier = Modifier.constrainAs(skipNext) {
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
                start.linkTo(playPause.end)
                end.linkTo(parent.end)
            },
            onClick = onSkipNextClicked
        ) {
            Icon(asset = vectorResource(id = R.drawable.ic_skip_next))
        }
    }
}
