package dev.andrewbailey.music.ui.player

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.andrewbailey.encore.player.state.MediaPlayerState
import dev.andrewbailey.encore.player.state.PlaybackState
import dev.andrewbailey.music.R
import dev.andrewbailey.music.model.Song
import dev.andrewbailey.music.ui.data.LocalPlaybackController

@Composable
fun NowPlayingControls(
    playbackState: MediaPlayerState<Song>?,
    modifier: Modifier = Modifier
) {
    if (playbackState is MediaPlayerState.Prepared) {
        val playbackController = LocalPlaybackController.current
        val transportState = playbackState.transportState
        val nowPlaying = transportState.queue.nowPlaying.mediaItem
        NowPlayingControls(
            songTitle = nowPlaying.name,
            albumName = nowPlaying.album?.name ?: stringResource(R.string.unknown_album),
            artistName = nowPlaying.artist?.name ?: stringResource(R.string.unknown_artist),
            songDurationMs = playbackState.durationMs ?: 0,
            seekPositionMs = transportState.seekPosition.seekPositionMillis,
            isPlaying = transportState.status == PlaybackState.PLAYING,
            onSkipNext = playbackController::skipNext,
            onSkipPrevious = playbackController::skipPrevious,
            onPlay = playbackController::play,
            onPause = playbackController::pause,
            onSeekToPositionMs = playbackController::seekTo,
            modifier = modifier
        )
    } else {
        NowPlayingControls(
            songTitle = stringResource(R.string.nothing_playing),
            albumName = "",
            artistName = "",
            songDurationMs = 1L,
            seekPositionMs = 0L,
            isPlaying = false,
            enabled = false,
            modifier = modifier
        )
    }
}

@Composable
private fun NowPlayingControls(
    songTitle: String,
    albumName: String,
    artistName: String,
    songDurationMs: Long,
    seekPositionMs: Long,
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onSkipNext: () -> Unit = {},
    onSkipPrevious: () -> Unit = {},
    onPlay: () -> Unit = {},
    onPause: () -> Unit = {},
    onSeekToPositionMs: (Long) -> Unit = {}
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceAround,
        modifier = modifier
    ) {
        SongMetadata(
            songTitle = songTitle,
            albumName = albumName,
            artistName = artistName,
            enabled = enabled,
            modifier = Modifier.weight(1f, fill = false)
        )

        SeekSlider(
            songDurationMs = songDurationMs,
            seekPositionMs = seekPositionMs,
            onSeekToPositionMs = onSeekToPositionMs,
            enabled = enabled,
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = 4.dp)
                .height(36.dp)
                .weight(1f, fill = false)
        )

        TransportControls(
            isPlaying = isPlaying,
            onSkipNext = onSkipNext,
            onSkipPrevious = onSkipPrevious,
            onPlay = onPlay,
            onPause = onPause,
            enabled = enabled,
            modifier = Modifier.fillMaxWidth(0.85f)
                .weight(1f, fill = false)
        )
    }
}

@Composable
private fun SongMetadata(
    songTitle: String,
    albumName: String,
    artistName: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val textColor = MaterialTheme.colors.onBackground.copy(
        alpha = if (enabled) 1f else 0.38f
    )

    Column(modifier = modifier) {
        Text(
            text = songTitle,
            style = MaterialTheme.typography.body1,
            color = textColor,
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = albumName,
            style = MaterialTheme.typography.body2,
            color = textColor,
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = artistName,
            style = MaterialTheme.typography.body2,
            color = textColor,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun SeekSlider(
    songDurationMs: Long,
    seekPositionMs: Long,
    onSeekToPositionMs: (Long) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val userSeekPosition = remember { mutableStateOf<Float?>(null) }
    val sliderInteractionState = remember { MutableInteractionSource() }

    Slider(
        valueRange = 0f..songDurationMs.toFloat(),
        interactionSource = sliderInteractionState,
        value = userSeekPosition.value
            .takeIf { sliderInteractionState.collectIsDraggedAsState().value }
            ?: seekPositionMs.toFloat(),
        onValueChange = { userSeekPosition.value = it },
        onValueChangeFinished = {
            onSeekToPositionMs(
                checkNotNull(userSeekPosition.value) {
                    "Failed to finalize seek because the requested seek position is unknown."
                }.toLong()
            )
            userSeekPosition.value = null
        },
        enabled = enabled,
        modifier = modifier
    )
}

@Composable
private fun TransportControls(
    isPlaying: Boolean,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onPlay: () -> Unit,
    onPause: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
    ) {
        TransportButton(
            painter = painterResource(R.drawable.ic_skip_previous),
            label = stringResource(R.string.content_description_skip_previous),
            onClick = onSkipPrevious,
            enabled = enabled,
            modifier = Modifier.weight(1.0f)
        )

        TransportButton(
            painter = painterResource(
                id = if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play
            ),
            label = stringResource(
                id = if (isPlaying) {
                    R.string.content_description_pause
                } else {
                    R.string.content_description_play
                }
            ),
            onClick = if (isPlaying) { onPause } else { onPlay },
            enabled = enabled,
            modifier = Modifier.weight(1.0f)
        )
        TransportButton(
            painter = painterResource(R.drawable.ic_skip_next),
            label = stringResource(R.string.content_description_skip_next),
            onClick = onSkipNext,
            enabled = enabled,
            modifier = Modifier.weight(1.0f)
        )
    }
}

@Composable
private fun TransportButton(
    painter: Painter,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        IconButton(
            onClick = onClick,
            enabled = enabled
        ) {
            Icon(
                painter = painter,
                contentDescription = label,
                tint = MaterialTheme.colors.onBackground
            )
        }
    }
}
