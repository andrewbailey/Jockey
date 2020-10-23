package dev.andrewbailey.music.ui.library

import androidx.compose.foundation.Icon
import androidx.compose.foundation.Image
import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.*
import androidx.compose.material.IconButton
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageAsset
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.viewModel
import dev.andrewbailey.encore.player.state.MediaPlayerState
import dev.andrewbailey.encore.player.state.PlaybackState
import dev.andrewbailey.music.R
import dev.andrewbailey.music.ui.root.PlaybackViewModel
import dev.andrewbailey.music.util.observe

@Composable
fun CollapsedPlayerControls(
    modifier: Modifier = Modifier
) {
    val viewModel = viewModel<PlaybackViewModel>()
    val playbackState = observe(viewModel.playbackState) as? MediaPlayerState.Prepared

    Box(modifier = modifier.preferredHeight(56.dp)) {
        Surface(elevation = 32.dp) {
            ConstraintLayout(
                modifier = Modifier.fillMaxWidth()
            ) {
                val artwork = createRef()
                val title = createRef()
                val artist = createRef()
                val seekBar = createRef()

                val (skipPrevious, playPause, skipNext) = createRefs()

                createHorizontalChain(
                    elements = arrayOf(skipPrevious, playPause, skipNext),
                    chainStyle = ChainStyle.Packed(bias = 1.0f)
                )

                createVerticalChain(
                    elements = arrayOf(title, artist),
                    chainStyle = ChainStyle.Packed
                )

                Box(
                    modifier = Modifier.constrainAs(artwork) {
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                        start.linkTo(parent.start)
                        width = Dimension.value(56.dp)
                        height = Dimension.value(56.dp)
                    }
                ) {
                    playbackState?.artwork?.let {
                        Image(asset = it.asImageAsset())
                    }
                }

                val nowPlaying = playbackState?.transportState?.queue?.nowPlaying?.mediaItem
                Text(
                    text = nowPlaying?.name.orEmpty(),
                    style = MaterialTheme.typography.body2,
                    modifier = Modifier.constrainAs(title) {
                        top.linkTo(seekBar.bottom)
                        bottom.linkTo(artist.top)
                        start.linkTo(artwork.end, 8.dp)
                        end.linkTo(skipPrevious.start, 8.dp)
                        width = Dimension.fillToConstraints
                    }
                )

                Text(
                    text = nowPlaying?.artist?.name.orEmpty(),
                    style = MaterialTheme.typography.caption,
                    modifier = Modifier.constrainAs(artist) {
                        top.linkTo(title.bottom)
                        bottom.linkTo(parent.bottom)
                        start.linkTo(artwork.end, 8.dp)
                        end.linkTo(skipPrevious.start, 8.dp)
                        width = Dimension.fillToConstraints
                    }
                )

                LinearProgressIndicator(
                    progress = playbackState?.durationMs?.let { duration ->
                        val percent = playbackState.transportState.seekPosition
                            .seekPositionMillis.toFloat() / duration

                        percent.coerceIn(0f..1f)
                    } ?: 0f,
                    color = MaterialTheme.colors.secondary,
                    modifier = Modifier.constrainAs(seekBar) {
                        top.linkTo(parent.top)
                        start.linkTo(artwork.end)
                        end.linkTo(parent.end)
                        width = Dimension.fillToConstraints
                    }
                )

                IconButton(
                    modifier = Modifier.constrainAs(skipPrevious) {
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                    },
                    onClick = viewModel::skipPrevious
                ) {
                    Icon(asset = vectorResource(id = R.drawable.ic_skip_previous))
                }

                val isPlaying = playbackState?.transportState?.status == PlaybackState.PLAYING
                IconButton(
                    modifier = Modifier.constrainAs(playPause) {
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                    },
                    onClick = {
                        if (isPlaying) {
                            viewModel.pause()
                        } else {
                            viewModel.play()
                        }
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
                    onClick = viewModel::skipNext
                ) {
                    Icon(asset = vectorResource(id = R.drawable.ic_skip_next))
                }
            }
        }
    }
}
