package dev.andrewbailey.music.ui.library

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.andrewbailey.encore.player.state.MediaPlayerState
import dev.andrewbailey.encore.player.state.hasContent
import dev.andrewbailey.encore.player.state.isPlaying
import dev.andrewbailey.encore.player.state.nowPlaying
import dev.andrewbailey.music.R
import dev.andrewbailey.music.model.Song
import dev.andrewbailey.music.ui.data.LocalPlaybackController
import dev.andrewbailey.music.util.collectAsNonUniqueState

private val controlBarHeight = 50.dp

@Composable
fun CollapsedPlayerControls(
    modifier: Modifier = Modifier
) {
    val playbackController = LocalPlaybackController.current
    val playbackState by playbackController.playbackState.collectAsNonUniqueState()
    val lastActivePlaybackState by remember {
        var activeState: MediaPlayerState.Prepared<Song>? = null
        derivedStateOf {
            playbackState?.let { state ->
                if (state.hasContent()) {
                    activeState = state
                }
            }
            return@derivedStateOf activeState
        }
    }

    val visibilityTransition = updateTransition(
        playbackState is MediaPlayerState.Prepared,
        label = "ControlsVisibilityTransition"
    )
    val contentOpacity by visibilityTransition.animateFloat(
        label = "ContentOpacityAnimation",
        transitionSpec = {
            if (targetState.isTransitioningTo(true)) {
                tween(
                    easing = LinearOutSlowInEasing,
                    durationMillis = 175,
                    delayMillis = 75
                )
            } else {
                tween(
                    easing = FastOutLinearInEasing,
                    durationMillis = 175
                )
            }
        }
    ) { controlsVisible ->
        if (controlsVisible) 1f else 0f
    }

    val contentHeight by visibilityTransition.animateDp(
        label = "ContentHeightAnimation",
        transitionSpec = {
            if (targetState.isTransitioningTo(true)) {
                tween(
                    easing = LinearOutSlowInEasing,
                    durationMillis = 250
                )
            } else {
                tween(
                    easing = FastOutLinearInEasing,
                    durationMillis = 250
                )
            }
        }
    ) { controlsVisible ->
        if (controlsVisible) controlBarHeight else 0.dp
    }

    Box(modifier = modifier.requiredHeight(contentHeight)) {
        lastActivePlaybackState?.let { state ->
            PopulatedCollapsedPlaybackControls(
                playbackState = state,
                modifier = Modifier
                    .height(controlBarHeight)
                    .offset(y = (controlBarHeight - contentHeight) / 2)
                    .alpha(contentOpacity)
                    .fillMaxWidth(),
                onPlayClicked = playbackController::play,
                onPauseClicked = playbackController::pause,
                onSkipNextClicked = playbackController::skipNext
            )
        }
    }
}

@Composable
private fun PopulatedCollapsedPlaybackControls(
    playbackState: MediaPlayerState.Prepared<Song>,
    modifier: Modifier = Modifier,
    onPlayClicked: () -> Unit = {},
    onPauseClicked: () -> Unit = {},
    onSkipNextClicked: () -> Unit = {}
) {
    Box(modifier = modifier) {
        SeekBar(playbackState)

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(horizontal = 4.dp)
                .padding(top = 2.dp)
        ) {
            AlbumArtwork(playbackState)

            SongTitleAndArtist(
                playbackState = playbackState,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            )

            PlayPauseButton(
                playbackState = playbackState,
                onPlayClicked = onPlayClicked,
                onPauseClicked = onPauseClicked
            )

            SkipButton(
                onSkipNextClicked = onSkipNextClicked
            )
        }
    }
}

@Composable
private fun SeekBar(
    playbackState: MediaPlayerState.Prepared<Song>,
    modifier: Modifier = Modifier
) {
    LinearProgressIndicator(
        progress = playbackState.durationMs?.let { duration ->
            val percent = playbackState.transportState.seekPosition
                .seekPositionMillis.toFloat() / duration

            percent.coerceIn(0f..1f)
        } ?: 0f,
        color = MaterialTheme.colors.secondary,
        modifier = modifier
            .height(3.dp)
            .fillMaxWidth()
            .clipToBounds()
    )
}

@Composable
private fun AlbumArtwork(
    playbackState: MediaPlayerState.Prepared<Song>,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(width = 40.dp, height = 40.dp)
            .border(
                width = 1.dp,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.15f),
                shape = RoundedCornerShape(size = 4.dp)
            )
    ) {
        Crossfade(
            targetState = playbackState.artwork,
            animationSpec = tween(easing = LinearEasing)
        ) { artwork ->
            if (artwork != null) {
                Image(
                    bitmap = artwork.asImageBitmap(),
                    contentDescription = stringResource(R.string.content_description_album_art),
                    modifier = Modifier.clip(RoundedCornerShape(size = 4.dp))
                )
            }
        }
    }
}

@Composable
private fun SongTitleAndArtist(
    playbackState: MediaPlayerState.Prepared<Song>,
    modifier: Modifier = Modifier
) {
    val nowPlaying = playbackState.nowPlaying().mediaItem
    Text(
        text = buildAnnotatedString {
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
        modifier = modifier
    )
}

@Composable
private fun PlayPauseButton(
    playbackState: MediaPlayerState.Prepared<Song>,
    modifier: Modifier = Modifier,
    onPlayClicked: () -> Unit = {},
    onPauseClicked: () -> Unit = {}
) {
    IconButton(
        modifier = modifier,
        onClick = if (playbackState.isPlaying()) {
            onPauseClicked
        } else {
            onPlayClicked
        }
    ) {
        Icon(
            painter = painterResource(
                id = if (playbackState.isPlaying()) {
                    R.drawable.ic_pause
                } else {
                    R.drawable.ic_play
                }
            ),
            contentDescription = stringResource(
                if (playbackState.isPlaying()) {
                    R.string.content_description_pause
                } else {
                    R.string.content_description_play
                }
            )
        )
    }
}

@Composable
private fun SkipButton(
    modifier: Modifier = Modifier,
    onSkipNextClicked: () -> Unit = {}
) {
    IconButton(
        modifier = modifier,
        onClick = onSkipNextClicked
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_skip_next),
            contentDescription = stringResource(R.string.content_description_skip_next)
        )
    }
}
