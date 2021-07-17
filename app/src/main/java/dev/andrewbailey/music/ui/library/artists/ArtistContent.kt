package dev.andrewbailey.music.ui.library.artists

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.andrewbailey.music.model.Album
import dev.andrewbailey.music.model.Artist
import dev.andrewbailey.music.model.Song
import dev.andrewbailey.music.ui.library.common.albums
import dev.andrewbailey.music.ui.library.common.songs
import dev.andrewbailey.music.ui.navigation.AlbumScreen
import dev.andrewbailey.music.ui.navigation.LocalAppNavigator
import dev.andrewbailey.music.ui.root.PlaybackViewModel
import dev.andrewbailey.music.util.rememberPaletteCache

@Composable
fun ArtistContent(
    artist: Artist,
    songs: List<Song>?,
    albums: List<Album>?,
    modifier: Modifier = Modifier
) {
    if (songs == null || albums == null) {
        ArtistLoadingState(
            modifier = modifier
        )
    } else {
        ArtistContentList(
            artist = artist,
            songs = songs,
            albums = albums,
            modifier = modifier
        )
    }
}

@Composable
private fun ArtistLoadingState(
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        CircularProgressIndicator(
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
private fun ArtistContentList(
    artist: Artist,
    songs: List<Song>,
    albums: List<Album>,
    modifier: Modifier = Modifier
) {
    val playbackViewModel = viewModel<PlaybackViewModel>()
    val navigator = LocalAppNavigator.current

    LazyColumn(
        modifier = modifier
    ) {
        item {
            val paletteCache = rememberPaletteCache()
            LazyRow(
                modifier = Modifier.height(180.dp),
                contentPadding = PaddingValues(all = 8.dp)
            ) {
                albums(
                    albums = albums,
                    paletteCache = paletteCache,
                    gridPadding = 8.dp,
                    onClickAlbum = { _, album ->
                        navigator.push(AlbumScreen(album))
                    }
                )
            }
        }

        songs(
            songs = songs,
            onClickSong = { index, _ ->
                playbackViewModel.playFrom(songs, index)
            }
        )
    }
}
