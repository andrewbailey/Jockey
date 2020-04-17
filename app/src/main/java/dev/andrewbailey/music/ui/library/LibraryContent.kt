package dev.andrewbailey.music.ui.library

import android.content.Context
import android.widget.FrameLayout
import androidx.compose.Composable
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.ui.core.Modifier
import androidx.ui.core.setContent
import androidx.ui.foundation.Text
import androidx.ui.foundation.VerticalScroller
import androidx.ui.graphics.Color
import androidx.ui.layout.Column
import androidx.ui.material.*
import androidx.ui.res.stringResource
import dev.andrewbailey.encore.model.MediaAuthor
import dev.andrewbailey.encore.model.MediaCollection
import dev.andrewbailey.encore.model.MediaItem
import dev.andrewbailey.encore.model.QueueItem
import dev.andrewbailey.encore.player.controller.EncoreController
import dev.andrewbailey.encore.player.state.*
import dev.andrewbailey.music.R
import dev.andrewbailey.music.ui.player.CollapsedPlayerControls
import dev.andrewbailey.music.util.fromRes
import dev.andrewbailey.music.util.observe
import java.util.*
import java.util.concurrent.TimeUnit

/*
 * Jetpack Compose is currently incompatible with Dagger. If you attempt to inject into a class
 * that has any functions annotated with @Compose, you will get an internal compiler error. This
 * appears to be related to Java interoperability with either the Compose compiler itself or with
 * the experimental IR-based compiler backend that Compose depends on.
 *
 * TODO: Once the Compose compiler is able to generate code compatible with Dagger, remove this
 *  wrapper function and move the @Composable functions into LibraryFragment
 */
fun LibraryContentView(
    context: Context,
    songs: LiveData<List<MediaItem>>,
    mediaController: EncoreController
) = FrameLayout(context).apply {
    setContent {
        LibraryContent(context, songs, mediaController)
    }
}

@Composable
private fun LibraryContent(
    context: Context,
    songs: LiveData<List<MediaItem>>,
    mediaController: EncoreController
) {
    val playbackState = mediaController.observeState(
        seekUpdateFrequency = EncoreController.SeekUpdateFrequency.WhilePlayingEvery(
            100,
            TimeUnit.MILLISECONDS
        )
    ).asLiveData()

    MaterialTheme(colorPalette(context)) {
        Surface {
            Column {
                TopAppBar(
                    title = { Text(stringResource(R.string.app_name)) }
                )

                Surface(Modifier.weight(1f)) {
                    val library = observe(songs)
                    SongList(library.orEmpty().sortedBy { it.name }, mediaController)
                }

                CollapsedPlayerControls(
                    encoreController = mediaController,
                    playbackState = observe(playbackState) as? MediaPlayerState.Prepared
                )
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

private fun colorPalette(context: Context) = lightColorPalette(
    primary = Color.fromRes(context, R.color.colorPrimary),
    primaryVariant = Color.fromRes(context, R.color.colorPrimary),
    secondary = Color.fromRes(context, R.color.colorAccent),
    secondaryVariant = Color.fromRes(context, R.color.colorAccentDark)
)
