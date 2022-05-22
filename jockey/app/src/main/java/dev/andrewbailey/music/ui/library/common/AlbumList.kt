package dev.andrewbailey.music.ui.library.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.palette.graphics.Target
import androidx.palette.graphics.get
import coil.compose.rememberAsyncImagePainter
import coil.imageLoader
import dev.andrewbailey.music.R
import dev.andrewbailey.music.model.Album
import dev.andrewbailey.music.ui.data.LocalColorRepository
import dev.andrewbailey.music.ui.data.rememberPaletteAsStateOf
import dev.andrewbailey.music.util.copy
import dev.andrewbailey.music.util.luminance

@Composable
fun AlbumList(
    albums: List<Album>,
    modifier: Modifier = Modifier,
    gridPadding: Dp = 4.dp,
    onClickAlbum: ((index: Int, album: Album) -> Unit)? = null
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 140.dp),
        contentPadding = PaddingValues(all = gridPadding / 2),
        modifier = modifier
    ) {
        albums(
            albums = albums,
            gridPadding = gridPadding,
            onClickAlbum = onClickAlbum
        )
    }
}

fun LazyGridScope.albums(
    albums: List<Album>,
    gridPadding: Dp = 4.dp,
    onClickAlbum: ((index: Int, album: Album) -> Unit)? = null
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

fun LazyListScope.albums(
    albums: List<Album>,
    gridPadding: Dp = 4.dp,
    onClickAlbum: ((index: Int, album: Album) -> Unit)? = null
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

@Composable
private fun AlbumCell(
    album: Album,
    modifier: Modifier = Modifier
) {
    val albumPalette by LocalColorRepository.current.rememberPaletteAsStateOf(album)
    val primaryColor = albumPalette?.let { palette ->
        maxOf(
            palette[Target.DARK_VIBRANT],
            palette[Target.LIGHT_VIBRANT],
            palette[Target.VIBRANT],
        ) { first, second ->
            (first?.population ?: Int.MIN_VALUE).compareTo(second?.population ?: Int.MIN_VALUE)
        }
    }

    val shimColor = primaryColor?.rgb?.let { Color(it) } ?: MaterialTheme.colors.surface
    val onShimColor = if (shimColor.luminance > 0.5f) {
        shimColor.copy(luminance = 0.1f)
    } else {
        shimColor.copy(luminance = 0.9f)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1.0f)
            .clip(MaterialTheme.shapes.medium)
            .shadow(5.dp)
    ) {
        Image(
            painter = rememberAsyncImagePainter(album, LocalContext.current.imageLoader),
            contentDescription = stringResource(id = R.string.content_description_album_art),
            modifier = Modifier
                .background(Color.Black)
                .fillMaxSize()
        )

        Column(
            verticalArrangement = Arrangement.Bottom,
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0.2f to shimColor.copy(alpha = 0.0f),
                        0.8f to shimColor.copy(alpha = 0.8f),
                        1.0f to shimColor.copy(alpha = 1.0f)
                    )
                )
                .padding(8.dp)
        ) {
            Text(
                text = album.name,
                style = MaterialTheme.typography.h6,
                color = onShimColor,
                overflow = TextOverflow.Ellipsis,
                maxLines = 2
            )

            Text(
                text = album.artist?.name.orEmpty(),
                style = MaterialTheme.typography.body2,
                color = onShimColor,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )
        }
    }
}
