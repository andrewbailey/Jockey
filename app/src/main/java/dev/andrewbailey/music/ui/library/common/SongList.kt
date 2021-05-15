package dev.andrewbailey.music.ui.library.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.andrewbailey.music.model.Album
import dev.andrewbailey.music.model.Artist
import dev.andrewbailey.music.model.Song

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SongList(
    songs: List<Song>,
    modifier: Modifier = Modifier,
    onClickSong: ((index: Int, song: Song) -> Unit)? = null
) {
    LazyColumn(
        modifier = modifier
    ) {
        itemsIndexed(
            items = songs,
            itemContent = { index, song ->
                ListItem(
                    text = {
                        Text(
                            text = song.name,
                            maxLines = 1
                        )
                    },
                    secondaryText = {
                        Text(
                            text = formattedAlbumArtist(song.album, song.artist),
                            maxLines = 1
                        )
                    },
                    modifier = if (onClickSong != null) {
                        Modifier.clickable(onClick = { onClickSong(index, song) })
                    } else {
                        Modifier
                    }
                )
                Divider()
            }
        )
    }
}

private fun formattedAlbumArtist(album: Album?, artist: Artist?): String =
    listOfNotNull(album?.name, artist?.name).joinToString(" - ")
