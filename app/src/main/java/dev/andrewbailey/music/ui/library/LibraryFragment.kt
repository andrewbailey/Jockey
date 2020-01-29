package dev.andrewbailey.music.ui.library

import android.widget.Toast
import androidx.compose.Composable
import androidx.compose.unaryPlus
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.ui.core.Text
import androidx.ui.foundation.VerticalScroller
import androidx.ui.graphics.Color
import androidx.ui.layout.Column
import androidx.ui.material.*
import androidx.ui.material.surface.Surface
import dev.andrewbailey.encore.model.MediaAuthor
import dev.andrewbailey.encore.model.MediaCollection
import dev.andrewbailey.encore.model.MediaItem
import dev.andrewbailey.encore.provider.mediastore.MediaStoreProvider
import dev.andrewbailey.music.R
import dev.andrewbailey.music.ui.ComposableFragment
import dev.andrewbailey.music.util.fromRes
import dev.andrewbailey.music.util.observe
import kotlinx.coroutines.launch

class LibraryFragment : ComposableFragment() {

    private val mediaProvider by lazy {
        MediaStoreProvider(requireContext())
    }

    @Composable
    override fun onCompose() {
        val songs = MutableLiveData<List<MediaItem>>()

        viewLifecycleOwner.lifecycleScope.launch {
            songs.postValue(mediaProvider.getAllMedia())
        }

        MaterialTheme(colorPalette()) {
            Surface {
                Column {
                    TopAppBar(
                        title = { Text(getString(R.string.app_name)) }
                    )

                    Surface {
                        val library = +observe(songs)
                        SongList(library.orEmpty().sortedBy { it.name })
                    }
                }
            }
        }
    }

    @Composable
    private fun SongList(songs: List<MediaItem>) {
        VerticalScroller {
            Column {
                songs.forEach { song ->
                    ListItem(
                        text = song.name,
                        secondaryText = formattedAlbumArtist(song.collection, song.author),
                        onClick = {
                            Toast.makeText(requireContext(), "Clicked $song", Toast.LENGTH_LONG)
                                .show()
                        }
                    )
                    Divider()
                }
            }
        }
    }

    private fun formattedAlbumArtist(album: MediaCollection?, artist: MediaAuthor?): String =
        listOfNotNull(album?.name, artist?.name).joinToString(" - ")

    private fun colorPalette() = ColorPalette(
        primary = Color.fromRes(requireContext(), R.color.colorPrimary),
        primaryVariant = Color.fromRes(requireContext(), R.color.colorPrimary),
        secondary = Color.fromRes(requireContext(), R.color.colorAccent),
        secondaryVariant = Color.fromRes(requireContext(), R.color.colorAccentDark)
    )

}
