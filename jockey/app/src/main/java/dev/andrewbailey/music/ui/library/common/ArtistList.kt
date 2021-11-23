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
import dev.andrewbailey.music.model.Artist

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ArtistList(
    artists: List<Artist>,
    modifier: Modifier = Modifier,
    onClickArtist: ((index: Int, artists: Artist) -> Unit)? = null
) {
    LazyColumn(
        modifier = modifier
    ) {
        itemsIndexed(
            items = artists,
            itemContent = { index, artists ->
                ListItem(
                    text = {
                        Text(
                            text = artists.name,
                            maxLines = 1
                        )
                    },
                    modifier = if (onClickArtist != null) {
                        Modifier.clickable(onClick = { onClickArtist(index, artists) })
                    } else {
                        Modifier
                    }
                )
                Divider()
            }
        )
    }
}
