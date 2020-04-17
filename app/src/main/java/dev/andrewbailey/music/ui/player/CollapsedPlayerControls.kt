package dev.andrewbailey.music.ui.player

import androidx.compose.Composable
import androidx.ui.core.Modifier
import androidx.ui.core.tag
import androidx.ui.foundation.Box
import androidx.ui.foundation.Icon
import androidx.ui.foundation.Image
import androidx.ui.foundation.Text
import androidx.ui.graphics.asImageAsset
import androidx.ui.layout.*
import androidx.ui.material.IconButton
import androidx.ui.material.LinearProgressIndicator
import androidx.ui.material.MaterialTheme
import androidx.ui.material.Surface
import androidx.ui.res.vectorResource
import androidx.ui.unit.dp
import dev.andrewbailey.encore.player.controller.EncoreController
import dev.andrewbailey.encore.player.state.MediaPlayerState
import dev.andrewbailey.encore.player.state.PlaybackState
import dev.andrewbailey.music.R

@Composable
fun CollapsedPlayerControls(
    encoreController: EncoreController,
    playbackState: MediaPlayerState.Prepared?
) {
    Box(modifier = Modifier.preferredHeight(56.dp)) {
        Surface(elevation = 32.dp) {
            ConstraintLayout(
                modifier = Modifier.fillMaxWidth(),
                constraintSet = ConstraintSet {
                    val artwork = tag("artwork")
                    val title = tag("title")
                    val artist = tag("artist")
                    val seekBar = tag("seekBar")

                    val playPauseButton = tag("playPause")
                    val skipNextButton = tag("skipNext")
                    val skipPrevButton = tag("skipPrev")

                    title.left constrainTo artwork.right
                    title.left.margin = 8.dp
                    title.right constrainTo skipPrevButton.left
                    artist constrainHorizontallyTo title
                    title.horizontalBias = 0f
                    artist.horizontalBias = 0f

                    seekBar constrainHorizontallyTo parent

                    createVerticalChain(
                        title,
                        artist,
                        chainStyle = ConstraintSetBuilderScope.ChainStyle.Packed
                    )

                    playPauseButton constrainVerticallyTo parent
                    skipNextButton constrainVerticallyTo parent
                    skipPrevButton constrainVerticallyTo parent

                    createHorizontalChain(
                        skipPrevButton,
                        playPauseButton,
                        skipNextButton,
                        chainStyle = ConstraintSetBuilderScope.ChainStyle.Packed(bias = 1.0f)
                    )
                },
                children = {
                    Box(modifier = Modifier.tag("artwork") + Modifier.preferredSize(56.dp, 56.dp)) {
                        playbackState?.artwork?.let {
                            Image(asset = it.asImageAsset())
                        }
                    }

                    val nowPlaying = playbackState?.transportState?.queue?.nowPlaying?.mediaItem
                    Text(
                        text = nowPlaying?.name.orEmpty(),
                        style = MaterialTheme.typography.body2,
                        modifier = Modifier.tag("title")
                    )
                    Text(
                        text = nowPlaying?.author?.name.orEmpty(),
                        style = MaterialTheme.typography.caption,
                        modifier = Modifier.tag("artist")
                    )

                    LinearProgressIndicator(
                        modifier = Modifier.tag("seekBar") +
                                Modifier.fillMaxWidth().padding(start = 56.dp),
                        progress = playbackState?.durationMs?.let { duration ->
                            val percent = playbackState.transportState.seekPosition
                                .seekPositionMillis.toFloat() / duration

                            percent.coerceIn(0f..1f)
                        } ?: 0f,
                        color = MaterialTheme.colors.secondary
                    )

                    if (playbackState?.transportState?.status == PlaybackState.PLAYING) {
                        IconButton(
                            modifier = Modifier.tag("playPause"),
                            onClick = encoreController::pause
                        ) {
                            Icon(asset = vectorResource(id = R.drawable.ic_pause))
                        }
                    } else {
                        IconButton(
                            modifier = Modifier.tag("playPause"),
                            onClick = encoreController::play
                        ) {
                            Icon(asset = vectorResource(id = R.drawable.ic_play))
                        }
                    }

                    IconButton(
                        modifier = Modifier.tag("skipNext"),
                        onClick = encoreController::skipNext
                    ) {
                        Icon(asset = vectorResource(id = R.drawable.ic_skip_next))
                    }

                    IconButton(
                        modifier = Modifier.tag("skipPrev"),
                        onClick = encoreController::skipPrevious
                    ) {
                        Icon(asset = vectorResource(id = R.drawable.ic_skip_previous))
                    }
                }
            )
        }
    }
}
