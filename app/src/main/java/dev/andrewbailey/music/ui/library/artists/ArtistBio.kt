package dev.andrewbailey.music.ui.library.artists

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.andrewbailey.music.R
import dev.andrewbailey.music.model.Album
import dev.andrewbailey.music.model.Artist
import dev.andrewbailey.music.model.Song
import dev.andrewbailey.music.ui.root.PlaybackViewModel
import dev.andrewbailey.music.util.pluralsResource
import dev.andrewbailey.music.util.rememberLetterPainter

@Composable
fun ArtistBio(
    artist: Artist,
    albums: List<Album>?,
    songs: List<Song>?,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .padding(16.dp)
    ) {
        ArtistPortrait(
            artist = artist,
            modifier = Modifier.align(Alignment.Top)
        )

        Column(
            modifier = Modifier.padding(start = 16.dp)
        ) {
            ArtistTitle(
                name = artist.name
            )

            if (albums != null && songs != null) {
                ArtistRuntimeInformation(
                    albums = albums,
                    songs = songs
                )
            }

            ShuffleAllButton(
                songs = songs,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ArtistPortrait(
    artist: Artist,
    modifier: Modifier = Modifier
) {
    Image(
        painter = rememberLetterPainter(
            text = artist.name.take(1),
            textSize = 64.sp
        ),
        contentDescription = null,
        modifier = modifier
            .size(128.dp)
            .clip(CircleShape)
    )
}

@Composable
private fun ArtistTitle(
    name: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = name,
        style = MaterialTheme.typography.h5,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
    )
}

@Composable
private fun ArtistRuntimeInformation(
    albums: List<Album>,
    songs: List<Song>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = listOfNotNull(
                albums.size.let { pluralsResource(R.plurals.album_count, it, it) },
                songs.size.let { pluralsResource(R.plurals.song_count, it, it) }
            ).joinToString(separator = " \u2022 "),
            style = MaterialTheme.typography.body2,
            color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = songs.sumOf { it.toMediaMetadata().durationMs ?: 0 }.let { totalDurationMs ->
                val seconds = (totalDurationMs / 1000) % 60
                val minutes = (totalDurationMs / 60_000) % 60
                val hours = totalDurationMs / 3_600_000

                when {
                    hours > 0 -> stringResource(R.string.duration_hr_min, hours, minutes)
                    else -> stringResource(R.string.duration_min_sec, minutes, seconds)
                }
            },
            style = MaterialTheme.typography.body2,
            color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ShuffleAllButton(
    songs: List<Song>?,
    modifier: Modifier = Modifier
) {
    if (songs?.isNotEmpty() == true) {
        val playbackViewModel = viewModel<PlaybackViewModel>()

        OutlinedButton(
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colors.secondary
            ),
            modifier = modifier.padding(top = 8.dp),
            onClick = {
                playbackViewModel.playShuffled(songs)
            }
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_shuffle),
                contentDescription = null
            )
            Text(
                text = stringResource(id = R.string.action_shuffle_all),
                maxLines = 1,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    } else {
        Spacer(modifier = modifier)
    }
}
