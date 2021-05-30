package dev.andrewbailey.music.ui.library.common

import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.palette.graphics.Target
import androidx.palette.graphics.get
import com.google.accompanist.coil.rememberCoilPainter
import dev.andrewbailey.music.R
import dev.andrewbailey.music.model.Album
import dev.andrewbailey.music.util.PaletteCache
import dev.andrewbailey.music.util.copy
import dev.andrewbailey.music.util.luminance
import dev.andrewbailey.music.util.rememberPalette
import dev.andrewbailey.music.util.rememberPaletteCache

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AlbumList(
    albums: List<Album>,
    modifier: Modifier = Modifier,
    gridPadding: Dp = 4.dp,
    onClickAlbum: ((index: Int, album: Album) -> Unit)? = null
) {
    val paletteCache = rememberPaletteCache()

    LazyVerticalGrid(
        cells = GridCells.Adaptive(minSize = 140.dp),
        contentPadding = PaddingValues(all = gridPadding / 2),
        modifier = modifier
    ) {
        itemsIndexed(albums) { index, album ->
            AlbumCell(
                album = album,
                paletteCache = paletteCache,
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
    modifier: Modifier = Modifier,
    paletteCache: PaletteCache? = null
) {
    val palette = rememberPalette(
        coilRequest = album,
        cache = paletteCache
    ).value?.let { palette ->
        maxOf(
            palette[Target.DARK_VIBRANT],
            palette[Target.LIGHT_VIBRANT],
            palette[Target.VIBRANT],
        ) { first, second ->
            (first?.population ?: Int.MIN_VALUE).compareTo(second?.population ?: Int.MIN_VALUE)
        }
    }

    val shimColor = palette?.rgb?.let { Color(it) } ?: MaterialTheme.colors.surface
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
            painter = rememberCoilPainter(
                request = album,
                fadeIn = true
            ),
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
