package dev.andrewbailey.music.ui.library.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.google.accompanist.coil.rememberCoilPainter
import dev.andrewbailey.music.model.Album
import dev.andrewbailey.music.model.Artist
import dev.andrewbailey.music.model.Song

@Composable
fun SongList(
    songs: List<Song>,
    modifier: Modifier = Modifier,
    onClickSong: ((index: Int, song: Song) -> Unit)? = null
) {
    LazyColumn(
        modifier = modifier
    ) {
        songs(songs, onClickSong)
    }
}

@OptIn(ExperimentalMaterialApi::class)
fun LazyListScope.songs(
    songs: List<Song>,
    onClickSong: ((index: Int, song: Song) -> Unit)? = null
) {
    itemsIndexed(
        items = songs,
        itemContent = { index, song ->
            ListItem(
                icon = {
                    Image(
                        painter = rememberCoilPainter(request = song),
                        contentDescription = null,
                        modifier = Modifier.size(40.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )
                },
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

private fun formattedAlbumArtist(album: Album?, artist: Artist?): String =
    listOfNotNull(album?.name, artist?.name).joinToString(" - ")
