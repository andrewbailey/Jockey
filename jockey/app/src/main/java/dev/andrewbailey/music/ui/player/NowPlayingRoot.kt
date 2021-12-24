package dev.andrewbailey.music.ui.player

import androidx.annotation.FloatRange
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.rememberInsetsPaddingValues
import com.google.accompanist.insets.statusBarsPadding
import dev.andrewbailey.encore.model.QueueItem
import dev.andrewbailey.encore.player.state.MediaPlayerState
import dev.andrewbailey.encore.player.state.ShuffleMode
import dev.andrewbailey.encore.player.state.TransportState
import dev.andrewbailey.music.R
import dev.andrewbailey.music.model.Song
import dev.andrewbailey.music.ui.data.LocalPlaybackController
import dev.andrewbailey.music.ui.data.PlaybackController
import dev.andrewbailey.music.ui.navigation.LocalAppNavigator
import dev.andrewbailey.music.util.collectAsNonUniqueState

@Composable
fun NowPlayingRoot(
    modifier: Modifier = Modifier,
    @FloatRange(from = 0.0, to = 1.0)
    percentVisible: Float = 1.0f
) {
    val playbackController = LocalPlaybackController.current
    val playbackState by playbackController.playbackState.collectAsNonUniqueState(null)

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

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun NowPlayingQueue(
    queue: List<QueueItem<Song>>,
    modifier: Modifier = Modifier
) {
    val playbackController = LocalPlaybackController.current

    Surface(
        elevation = 0.dp,
        color = MaterialTheme.colors.background,
        modifier = modifier
    ) {
        LazyColumn(
            contentPadding = rememberInsetsPaddingValues(LocalWindowInsets.current.navigationBars)
        ) {
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
                                    playbackController.playAtQueueIndex(index)
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
