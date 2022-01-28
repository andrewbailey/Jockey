package dev.andrewbailey.music.ui.player

import androidx.annotation.FloatRange
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.statusBarsPadding
import dev.andrewbailey.encore.player.state.MediaPlayerState
import dev.andrewbailey.encore.player.state.ShuffleMode
import dev.andrewbailey.encore.player.state.TransportState
import dev.andrewbailey.music.R
import dev.andrewbailey.music.model.Song
import dev.andrewbailey.music.ui.data.LocalPlaybackController
import dev.andrewbailey.music.ui.data.PlaybackController
import dev.andrewbailey.music.ui.layout.ModalScaffold
import dev.andrewbailey.music.ui.layout.ModalStateValue
import dev.andrewbailey.music.ui.layout.rememberModalState
import dev.andrewbailey.music.ui.navigation.LocalAppNavigator
import dev.andrewbailey.music.util.collectAsNonUniqueState
import kotlinx.coroutines.launch

@Composable
fun NowPlayingRoot(
    modifier: Modifier = Modifier,
    @FloatRange(from = 0.0, to = 1.0)
    percentVisible: Float = 1.0f
) {
    val playbackController = LocalPlaybackController.current
    val playbackState by playbackController.playbackState.collectAsNonUniqueState(null)

    val nowPlayingModalState = rememberModalState(ModalStateValue.Collapsed)
    val coroutineScope = rememberCoroutineScope()

    with(LocalAppNavigator.current) {
        PopBehavior {
            if (nowPlayingModalState.currentValue != ModalStateValue.Collapsed) {
                coroutineScope.launch {
                    nowPlayingModalState.collapse()
                }
                true
            } else {
                false
            }
        }
    }

    ModalScaffold(
        bodyContent = {
            NowPlayingContent(
                playbackState = playbackState,
                percentVisible = percentVisible,
                modifier = Modifier.fillMaxSize()
            )
        },
        sheetContent = {
            (playbackState?.transportState as? TransportState.Active)?.queue?.let { queue ->
                Surface(
                    elevation = 16.dp,
                    modifier = Modifier.topBorder()
                ) {
                    NowPlayingQueue(
                        queue = queue,
                        modifier = Modifier.fillMaxSize(),
                        percentExpanded = nowPlayingModalState.percentExpanded,
                        expandQueue = {
                            coroutineScope.launch {
                                nowPlayingModalState.expand()
                            }
                        },
                        collapseQueue = {
                            coroutineScope.launch {
                                nowPlayingModalState.collapse()
                            }
                        }
                    )
                }
            }
        },
        state = nowPlayingModalState,
        collapsedSheetHeightPx = nowPlayingQueueCollapsedHeightPx() +
            LocalWindowInsets.current.systemBars.bottom,
        maximumExpandedHeight = LocalConfiguration.current.screenHeightDp.dp - 128.dp,
        modifier = modifier
    )
}

@Composable
fun NowPlayingContent(
    playbackState: MediaPlayerState<Song>?,
    modifier: Modifier = Modifier,
    @FloatRange(from = 0.0, to = 1.0)
    percentVisible: Float = 1.0f
) {
    val playbackController = LocalPlaybackController.current

    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .background(Color.Black)
                .fillMaxWidth()
                .aspectRatio(1.0f)
        ) {
            NowPlayingToolbar(
                playbackController = playbackController,
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
                        .insetBottomShadow()
                )
            }
        }

        NowPlayingControls(
            playbackState = playbackState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(color = MaterialTheme.colors.surface)
                .padding(32.dp)
        )
    }
}

@Composable
private fun NowPlayingToolbar(
    playbackController: PlaybackController,
    playbackState: MediaPlayerState<Song>?,
    modifier: Modifier = Modifier
) {
    val navigator = LocalAppNavigator.current

    TopAppBar(
        modifier = modifier
            .zIndex(1f)
            .statusBarsPadding(),
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
                    onClick = { playbackController.setShuffleMode(toggledShuffleMode) }
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

private fun Modifier.insetBottomShadow(
    shadowHeight: Dp = 8.dp
): Modifier = composed {
    val heightPx = with(LocalDensity.current) { shadowHeight.toPx() }
    drawWithContent {
        drawContent()
        drawRect(
            brush = Brush.linearGradient(
                colorStops = arrayOf(
                    0.0f to Color.Black.copy(alpha = 0.00f),
                    0.5f to Color.Black.copy(alpha = 0.04f),
                    1.0f to Color.Black.copy(alpha = 0.12f)
                ),
                start = Offset(0f, size.height - heightPx),
                end = Offset(0f, size.height)
            )
        )
    }
}

private fun Modifier.topBorder() = composed {
    val color = MaterialTheme.colors.onSurface.copy(alpha = 0.2f)
    drawWithContent {
        drawContent()
        drawLine(
            color = color,
            start = Offset(0f, 0f),
            end = Offset(size.width, 0f)
        )
    }
}
