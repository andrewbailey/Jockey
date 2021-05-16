package dev.andrewbailey.music.ui.library.common

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.andrewbailey.music.R
import dev.andrewbailey.music.model.Album

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AlbumList(
    albums: List<Album>,
    modifier: Modifier = Modifier,
    gridPadding: Dp = 2.dp,
    onClickAlbum: ((index: Int, album: Album) -> Unit)? = null
) {
    LazyVerticalGrid(
        cells = GridCells.Adaptive(minSize = 120.dp),
        contentPadding = PaddingValues(all = gridPadding / 2),
        modifier = modifier
    ) {
        itemsIndexed(albums) { index, album ->
            AlbumCell(
                album = album,
                modifier = Modifier
                    .padding(all = gridPadding / 2)
                    .then(
                        other = if (onClickAlbum != null) {
                            Modifier.clickable(onClick = { onClickAlbum(index, album) })
                        } else {
                            Modifier
                        }
                    )
            )
        }
    }
}

@Composable
private fun AlbumCell(
    album: Album,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
    ) {
        Image(
            painter = ColorPainter(Color.Black),
            contentDescription = stringResource(id = R.string.content_description_album_art),
            modifier = Modifier
                .background(Color.Black)
                .aspectRatio(1.0f)
        )

        Text(
            text = album.name,
            color = Color.White
        )
    }
}
