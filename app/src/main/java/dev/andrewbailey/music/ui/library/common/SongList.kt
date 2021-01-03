package dev.andrewbailey.music.ui.library.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumnForIndexed
import androidx.compose.material.Divider
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.andrewbailey.encore.provider.mediastore.MediaStoreAlbum
import dev.andrewbailey.encore.provider.mediastore.MediaStoreArtist
import dev.andrewbailey.encore.provider.mediastore.MediaStoreSong

@Composable
fun SongList(
    songs: List<MediaStoreSong>,
    modifier: Modifier = Modifier,
    onClickSong: ((index: Int, song: MediaStoreSong) -> Unit)? = null
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

private fun formattedAlbumArtist(album: MediaStoreAlbum?, artist: MediaStoreArtist?): String =
    listOfNotNull(album?.name, artist?.name).joinToString(" - ")
