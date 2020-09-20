package dev.andrewbailey.music.ui.library

import androidx.compose.foundation.Box
import androidx.compose.foundation.Text
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumnForIndexed
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.fragment.app.viewModels
import androidx.lifecycle.asLiveData
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import dev.andrewbailey.encore.model.QueueItem
import dev.andrewbailey.encore.player.controller.EncoreController
import dev.andrewbailey.encore.player.state.*
import dev.andrewbailey.encore.provider.mediastore.LocalAlbum
import dev.andrewbailey.encore.provider.mediastore.LocalArtist
import dev.andrewbailey.encore.provider.mediastore.LocalSong
import dev.andrewbailey.music.R
import dev.andrewbailey.music.ui.ComposableFragment
import dev.andrewbailey.music.ui.core.colorPalette
import dev.andrewbailey.music.util.observe
import java.util.*
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class LibraryFragment : ComposableFragment() {

    private val viewModel: LibraryViewModel by viewModels()

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
        songs: List<LocalSong>,
        mediaController: EncoreController<LocalSong>
    ) {
        LazyColumnForIndexed(
            items = songs
        ) { index, song ->
            ListItem(
                text = {
                    Text(song.name)
                },
                secondaryText = {
                    Text(formattedAlbumArtist(song.album, song.artist))
                },
                modifier = Modifier
                    .clickable(onClick = {
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
                    })
            )
            Divider()
        }
    }

    private fun formattedAlbumArtist(album: LocalAlbum?, artist: LocalArtist?): String =
        listOfNotNull(album?.name, artist?.name).joinToString(" - ")

}
