package dev.andrewbailey.music.ui.library

import androidx.compose.animation.ColorPropKey
import androidx.compose.animation.DpPropKey
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.transitionDefinition
import androidx.compose.animation.core.tween
import androidx.compose.animation.transition
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.ripple.rememberRippleIndication
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import dev.andrewbailey.music.R
import dev.andrewbailey.music.ui.navigation.LibraryPage
import dev.andrewbailey.music.ui.navigation.LibraryPage.Albums
import dev.andrewbailey.music.ui.navigation.LibraryPage.Artists
import dev.andrewbailey.music.ui.navigation.LibraryPage.Folders
import dev.andrewbailey.music.ui.navigation.LibraryPage.Playlists
import dev.andrewbailey.music.ui.navigation.LibraryPage.Songs

@Composable
fun LibraryNavigationBar(
    selectedPage: LibraryPage,
    modifier: Modifier = Modifier,
    onSelectLibraryPage: (LibraryPage) -> Unit = {}
) {
    Row(
        modifier = modifier
            .height(56.dp)
            .padding(start = 12.dp, end = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        listOf(Playlists, Songs, Albums, Artists, Folders).forEach { page ->
            LibraryNavigationOption(
                page = page,
                isSelected = page == selectedPage,
                modifier = Modifier
                    .weight(1f, true),
                onSelected = onSelectLibraryPage
            )
        }
    }
}

@Composable
private fun LibraryNavigationOption(
    page: LibraryPage,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onSelected: (LibraryPage) -> Unit = {}
) {
    Column(
        modifier = modifier
            .clickable(
                indication = rememberRippleIndication(
                    bounded = false,
                    color = MaterialTheme.colors.primary
                ),
                onClick = { onSelected(page) }
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val previousSelectionState = remember { mutableStateOf(isSelected) }
        val selectionColor = MaterialTheme.colors.primary
        val unselectedColor = MaterialTheme.colors.onSurface.copy(
            alpha = if (MaterialTheme.colors.isLight) {
                0.5f
            } else {
                0.6f
            }
        )

        val iconColor = remember { ColorPropKey() }
        val textColor = remember { ColorPropKey() }
        val iconTranslation = remember { DpPropKey() }
        val textTranslation = remember { DpPropKey() }

        val selectionStateTransition = remember {
            transitionDefinition<Boolean> {
                state(true) {
                    this[textColor] = selectionColor
                    this[iconColor] = selectionColor
                    this[iconTranslation] = 0.dp
                    this[textTranslation] = 0.dp
                }

                state(false) {
                    this[textColor] = unselectedColor.copy(alpha = 0f)
                    this[iconColor] = unselectedColor
                    this[iconTranslation] = 8.dp
                    this[textTranslation] = 8.dp
                }

                transition(false to true) {
                    val durationMs = 200
                    val easing = LinearOutSlowInEasing
                    textColor using tween(
                        easing = easing,
                        durationMillis = durationMs
                    )
                    iconColor using tween(
                        easing = easing,
                        durationMillis = durationMs
                    )
                    iconTranslation using tween(
                        easing = easing,
                        durationMillis = durationMs
                    )
                    textTranslation using tween(
                        easing = easing,
                        durationMillis = durationMs
                    )
                }

                transition(true to false) {
                    val durationMs = 200
                    val easing = FastOutLinearInEasing
                    textColor using tween(
                        easing = easing,
                        durationMillis = durationMs
                    )
                    iconColor using tween(
                        easing = easing,
                        durationMillis = durationMs
                    )
                    iconTranslation using tween(
                        easing = easing,
                        durationMillis = durationMs
                    )
                    textTranslation using tween(
                        easing = easing,
                        durationMillis = durationMs
                    )
                }
            }
        }

        val transitionState = transition(
            definition = selectionStateTransition,
            initState = previousSelectionState.value,
            toState = isSelected,
            onStateChangeFinished = {
                previousSelectionState.value = it
            }
        )

        LibraryNavigationIcon(
            page = page,
            tint = transitionState[iconColor],
            modifier = Modifier
                .offset(y = transitionState[iconTranslation])
        )

        LibraryNavigationLabel(
            page = page,
            textColor = transitionState[textColor],
            modifier = Modifier.offset(y = transitionState[textTranslation])
        )
    }
}

@Composable
private fun LibraryNavigationIcon(
    page: LibraryPage,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Icon(
        imageVector = vectorResource(
            id = when (page) {
                Playlists -> R.drawable.ic_library_playlists
                Songs -> R.drawable.ic_library_songs
                Albums -> R.drawable.ic_library_albums
                Artists -> R.drawable.ic_library_artists
                Folders -> R.drawable.ic_library_folder
            }
        ),
        tint = tint,
        modifier = modifier
    )
}

@Composable
private fun LibraryNavigationLabel(
    page: LibraryPage,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Text(
        style = MaterialTheme.typography.caption,
        maxLines = 1,
        color = textColor,
        modifier = modifier,
        text = when (page) {
            Playlists -> stringResource(R.string.library_tab_playlists)
            Songs -> stringResource(R.string.library_tab_songs)
            Albums -> stringResource(R.string.library_tab_albums)
            Artists -> stringResource(R.string.library_tab_artists)
            Folders -> stringResource(R.string.library_tab_folders)
        }
    )
}
