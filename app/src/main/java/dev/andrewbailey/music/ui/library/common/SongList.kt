package dev.andrewbailey.music.ui.library.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumnForIndexed
import androidx.compose.material.Divider
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.andrewbailey.encore.provider.mediastore.LocalAlbum
import dev.andrewbailey.encore.provider.mediastore.LocalArtist
import dev.andrewbailey.encore.provider.mediastore.LocalSong

@Composable
fun SongList(
    songs: List<LocalSong>,
    modifier: Modifier = Modifier,
    onClickSong: ((index: Int, song: LocalSong) -> Unit)? = null
) {
    LazyColumnForIndexed(
        items = songs,
        modifier = modifier
    ) { index, song ->
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
}

private fun formattedAlbumArtist(album: LocalAlbum?, artist: LocalArtist?): String =
    listOfNotNull(album?.name, artist?.name).joinToString(" - ")
