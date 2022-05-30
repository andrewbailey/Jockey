package dev.andrewbailey.music.ui.library.playlists

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.ListItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.andrewbailey.encore.provider.mediastore.MediaStorePlaylist
import dev.andrewbailey.music.R
import dev.andrewbailey.music.ui.data.LocalMediaLibrary

@Composable
fun AllPlaylistsRoot(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    val mediaLibrary = LocalMediaLibrary.current
    val playlists = mediaLibrary.playlists.collectAsState().value

    if (playlists == null) {
        AllPlaylistsLoadingState(modifier)
    } else {
        val context = LocalContext.current
        Box(modifier) {
            FloatingActionButton(
                modifier = Modifier.align(Alignment.BottomEnd)
                    .offset(x = (-16).dp, y = (-16).dp)
                    .offset(
                        x = -contentPadding.calculateEndPadding(LocalLayoutDirection.current),
                        y = -contentPadding.calculateBottomPadding()
                    ),
                onClick = {
                    Toast.makeText(context, "Not implemented", Toast.LENGTH_SHORT).show()
                }
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_add_24),
                    contentDescription = stringResource(R.string.content_description_new_playlist)
                )
            }

            val contentModifier = Modifier.fillMaxSize()
            when {
                playlists.isEmpty() -> AllPlaylistsEmptyState(contentModifier)
                else -> PlaylistsList(
                    playlists = playlists,
                    modifier = contentModifier,
                    contentPadding = contentPadding,
                    onClickPlaylist = {

                    }
                )
            }
        }
    }
}

@Composable
private fun AllPlaylistsLoadingState(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun AllPlaylistsEmptyState(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            style = MaterialTheme.typography.subtitle1,
            textAlign = TextAlign.Center,
            text = stringResource(id = R.string.library_playlists_empty_header),
            modifier = Modifier.widthIn(max = 280.dp)
        )

        Text(
            style = MaterialTheme.typography.body2,
            textAlign = TextAlign.Center,
            text = stringResource(id = R.string.library_playlists_empty_description),
            modifier = Modifier.widthIn(max = 280.dp)
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun PlaylistsList(
    playlists: List<MediaStorePlaylist>,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    onClickPlaylist: ((playlist: MediaStorePlaylist) -> Unit)? = null
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = contentPadding
    ) {
        items(
            items = playlists,
            itemContent = { playlist ->
                ListItem(
                    text = {
                        Text(
                            text = playlist.name,
                            maxLines = 1
                        )
                    },
                    modifier = if (onClickPlaylist != null) {
                        Modifier.clickable(onClick = { onClickPlaylist(playlist) })
                    } else {
                        Modifier
                    }
                )
                Divider()
            }
        )
    }
}
