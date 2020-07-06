package dev.andrewbailey.music.ui.library

import android.os.Bundle
import androidx.compose.Composable
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.navigation.fragment.findNavController
import androidx.ui.core.Modifier
import androidx.ui.foundation.*
import androidx.ui.layout.Column
import androidx.ui.material.*
import androidx.ui.res.stringResource
import dev.andrewbailey.encore.model.MediaAuthor
import dev.andrewbailey.encore.model.MediaCollection
import dev.andrewbailey.encore.model.MediaItem
import dev.andrewbailey.encore.model.QueueItem
import dev.andrewbailey.encore.player.controller.EncoreController
import dev.andrewbailey.encore.player.state.*
import dev.andrewbailey.music.JockeyApplication
import dev.andrewbailey.music.R
import dev.andrewbailey.music.ui.ComposableFragment
import dev.andrewbailey.music.ui.core.colorPalette
import dev.andrewbailey.music.util.observe
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class LibraryFragment : ComposableFragment() {

    @Inject
    lateinit var viewModelProvider: ViewModelProvider.Factory

    private val viewModel by viewModels<LibraryViewModel> { viewModelProvider }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        JockeyApplication.getComponent(requireContext()).inject(this)
    }

    @Composable
    override fun onCompose() {
        val playbackState = viewModel.mediaController.observeState(
            seekUpdateFrequency = EncoreController.SeekUpdateFrequency.WhilePlayingEvery(
                100,
                TimeUnit.MILLISECONDS
            )
        ).asLiveData()

        MaterialTheme(colorPalette()) {
            Surface {
                Column {
                    TopAppBar(
                        title = { Text(stringResource(R.string.app_name)) }
                    )

                    Surface(Modifier.weight(1f)) {
                        val library = observe(viewModel.songs)
                        SongList(library.orEmpty().sortedBy { it.name }, viewModel.mediaController)
                    }

                    Box(
                        modifier = Modifier.clickable(onClick = {
                            findNavController().navigate(R.id.nowPlayingFragment)
                        })
                    ) {
                        CollapsedPlayerControls(
                            encoreController = viewModel.mediaController,
                            playbackState = observe(playbackState) as? MediaPlayerState.Prepared
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun SongList(
        songs: List<MediaItem>,
        mediaController: EncoreController
    ) {
        VerticalScroller {
            Column {
                songs.forEachIndexed { index, song ->
                    ListItem(
                        text = song.name,
                        secondaryText = formattedAlbumArtist(song.collection, song.author),
                        onClick = {
                            mediaController.setState(
                                TransportState.Active(
                                    status = PlaybackState.PLAYING,
                                    seekPosition = SeekPosition.AbsoluteSeekPosition(0),
                                    queue = QueueState.Linear(
                                        queue = songs.map {
                                            QueueItem(
                                                queueId = UUID.randomUUID(),
                                                mediaItem = it
                                            )
                                        },
                                        queueIndex = index
                                    ),
                                    repeatMode = RepeatMode.REPEAT_NONE
                                )
                            )
                        }
                    )
                    Divider()
                }
            }
        }
    }

    private fun formattedAlbumArtist(album: MediaCollection?, artist: MediaAuthor?): String =
        listOfNotNull(album?.name, artist?.name).joinToString(" - ")

}
