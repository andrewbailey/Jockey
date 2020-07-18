package dev.andrewbailey.music.ui.player

import android.os.Bundle
import androidx.compose.Composable
import androidx.compose.state
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.ui.core.*
import androidx.ui.foundation.*
import androidx.ui.foundation.lazy.LazyColumnItems
import androidx.ui.graphics.Color
import androidx.ui.graphics.LinearGradient
import androidx.ui.graphics.asImageAsset
import androidx.ui.layout.*
import androidx.ui.material.*
import androidx.ui.res.stringResource
import androidx.ui.res.vectorResource
import androidx.ui.unit.dp
import dev.andrewbailey.encore.model.MediaItem
import dev.andrewbailey.encore.model.QueueItem
import dev.andrewbailey.encore.player.state.*
import dev.andrewbailey.music.JockeyApplication
import dev.andrewbailey.music.R
import dev.andrewbailey.music.ui.ComposableFragment
import dev.andrewbailey.music.ui.core.colorPalette
import dev.andrewbailey.music.util.observe
import java.util.*
import javax.inject.Inject

class NowPlayingFragment : ComposableFragment() {

    @Inject
    lateinit var viewModelProvider: ViewModelProvider.Factory

    private val viewModel by viewModels<NowPlayingViewModel> { viewModelProvider }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        JockeyApplication.getComponent(requireContext()).inject(this)
    }

    @Composable
    override fun onCompose() {
        val playbackState = observe(viewModel.playbackState)

        MaterialTheme(colorPalette()) {
            ConstraintLayout(
                modifier = Modifier.fillMaxHeight()
            ) {
                val (toolbar, artwork, controls, queue) = createRefs()

                TopAppBar(
                    modifier = Modifier.constrainAs(toolbar) {
                        top.linkTo(parent.top)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    } + Modifier.zIndex(1f),
                    backgroundColor = Color.Transparent,
                    title = { Text(stringResource(R.string.page_title_now_playing)) },
                    elevation = 0.dp,
                    contentColor = Color.White,
                    navigationIcon = {
                        IconButton(
                            onClick = { findNavController().navigateUp() }
                        ) {
                            Icon(
                                asset = vectorResource(id = R.drawable.ic_close_24)
                            )
                        }
                    }
                )

                Box(
                    modifier = Modifier.constrainAs(artwork) {
                        top.linkTo(parent.top)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        width = Dimension.fillToConstraints
                        height = Dimension.wrapContent
                    } + Modifier.aspectRatio(1.0f) + Modifier.scrim(),
                    backgroundColor = Color.Black
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
                    modifier = Modifier.drawShadow(elevation = 4.dp) +
                            Modifier.zIndex(4f) +
                            Modifier.drawBackground(color = MaterialTheme.colors.surface) +
                            Modifier.padding(16.dp) +
                            Modifier.constrainAs(controls) {
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
    }

    @Composable
    private fun NowPlayingControls(
        playbackState: MediaPlayerState?,
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
        playbackState: MediaPlayerState.Prepared,
        modifier: Modifier
    ) {
        ConstraintLayout(
            modifier = modifier
        ) {
            val (songName, artistName, albumName) = createRefs()
            val (seekBar, skipPrevious, playPause, skipNext) = createRefs()

            createHorizontalChain(
                skipPrevious, playPause, skipNext,
                chainStyle = ChainStyle.Spread
            )

            createVerticalChain(
                songName, artistName, albumName, seekBar, playPause,
                chainStyle = ChainStyle.Packed
            )

            val userSeekPosition = state<Float?>(init = { null })

            val nowPlaying = playbackState.transportState.queue.nowPlaying.mediaItem
            Text(
                text = nowPlaying.name,
                style = MaterialTheme.typography.body1,
                modifier = Modifier.constrainAs(songName) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    width = Dimension.fillToConstraints
                }
            )

            Text(
                text = nowPlaying.collection?.name ?: "No album",
                style = MaterialTheme.typography.body2,
                modifier = Modifier.constrainAs(albumName) {
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    width = Dimension.fillToConstraints
                }
            )

            Text(
                text = nowPlaying.author?.name ?: "No artist",
                style = MaterialTheme.typography.body2,
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
                    viewModel.seekTo(checkNotNull(userSeekPosition.value) {
                        "Failed to finalize seek because the requested seek position is unknown."
                    }.toLong())
                    userSeekPosition.value = null
                },
                modifier = Modifier.constrainAs(seekBar) {
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                } + Modifier.offset(y = 4.dp) +
                        Modifier.preferredHeight(36.dp)
            )

            IconButton(
                onClick = viewModel::skipPrevious,
                modifier = Modifier.constrainAs(skipPrevious) {
                    top.linkTo(playPause.top)
                    bottom.linkTo(playPause.bottom)
                }
            ) {
                Icon(asset = vectorResource(id = R.drawable.ic_skip_previous))
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
                    Icon(asset = vectorResource(id = R.drawable.ic_pause))
                } else {
                    Icon(asset = vectorResource(id = R.drawable.ic_play))
                }
            }

            IconButton(
                onClick = viewModel::skipNext,
                modifier = Modifier.constrainAs(skipNext) {
                    top.linkTo(playPause.top)
                    bottom.linkTo(playPause.bottom)
                }
            ) {
                Icon(asset = vectorResource(id = R.drawable.ic_skip_next))
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
        queue: List<QueueItem>,
        modifier: Modifier = Modifier
    ) {
        Surface(
            elevation = 0.dp,
            color = MaterialTheme.colors.background,
            modifier = modifier
        ) {
            LazyColumnItems(
                items = queue.withIndex().toList()
            ) { (index, queueItem) ->
                ListItem(
                    text = queueItem.mediaItem.name,
                    secondaryText = formattedAlbumArtist(queueItem.mediaItem),
                    onClick = {
                        viewModel.playAtQueueIndex(index)
                    }
                )
                Divider()
            }
        }
    }

    private fun formattedAlbumArtist(item: MediaItem): String =
        listOfNotNull(item.collection?.name, item.author?.name).joinToString(" - ")

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

}
