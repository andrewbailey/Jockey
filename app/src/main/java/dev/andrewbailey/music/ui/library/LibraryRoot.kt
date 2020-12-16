package dev.andrewbailey.music.ui.library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.savedinstancestate.savedInstanceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import dev.andrewbailey.music.R
import dev.andrewbailey.music.ui.layout.BottomSheetScaffold
import dev.andrewbailey.music.ui.layout.CollapsingPageValue
import dev.andrewbailey.music.ui.layout.rememberCollapsingPageState
import dev.andrewbailey.music.ui.library.songs.AllSongsRoot
import dev.andrewbailey.music.ui.navigation.AppNavigator
import dev.andrewbailey.music.ui.navigation.LibraryPage
import dev.andrewbailey.music.ui.player.NowPlayingRoot

@Composable
fun LibraryRoot(
    modifier: Modifier = Modifier
) {
    val isFullPlaybackControlsVisible = savedInstanceState { false }
    val selectedPage = savedInstanceState { LibraryPage.Songs }

    val bottomSheetState = rememberCollapsingPageState(
        initialValue = if (isFullPlaybackControlsVisible.value) {
            CollapsingPageValue.expanded
        } else {
            CollapsingPageValue.collapsed
        }
    )

    AppNavigator.current.overridePopBehavior(
        navigateUp = {
            if (bottomSheetState.isFullyExpanded) {
                bottomSheetState.collapse()
                true
            } else {
                false
            }
        }
    )

    BottomSheetScaffold(
        modifier = modifier,
        state = bottomSheetState,
        bodyContent = {
            LibraryContent(
                page = selectedPage.value
            )
        },
        onStateChanged = {
            when (it) {
                CollapsingPageValue.expanded -> isFullPlaybackControlsVisible.value = true
                CollapsingPageValue.collapsed -> isFullPlaybackControlsVisible.value = false
            }
        },
        collapsedSheetLayout = {
            LibraryNavigation(
                selectedPage = selectedPage.value,
                modifier = Modifier
                    .alpha((1 - 2 * bottomSheetState.expansionPercentage).coerceIn(0f..1f)),
                showFullPlaybackControls = {
                    bottomSheetState.expand()
                },
                selectTab = { newPage ->
                    selectedPage.value = newPage
                }
            )
        },
        expandedSheetLayout = {
            NowPlayingRoot(
                percentVisible = bottomSheetState.expansionPercentage
            )
        }
    )
}

@Composable
private fun LibraryContent(
    page: LibraryPage,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        TopAppBar(
            title = { Text(stringResource(R.string.app_name)) }
        )

        Surface(Modifier.weight(1f)) {
            val contentModifier = Modifier.fillMaxSize()

            when (page) {
                LibraryPage.Songs -> AllSongsRoot(
                    modifier = contentModifier
                )
                else -> {
                    Box(
                        modifier = contentModifier,
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Not yet implemented.")
                    }
                }
            }
        }
    }
}

@Composable
private fun LibraryNavigation(
    selectedPage: LibraryPage,
    modifier: Modifier = Modifier,
    showFullPlaybackControls: () -> Unit = {},
    selectTab: (LibraryPage) -> Unit = {}
) {
    Column(
        modifier = modifier
    ) {
        Surface {
            CollapsedPlayerControls(
                modifier = Modifier.clickable(onClick = showFullPlaybackControls)
            )
        }

        Surface {
            LibraryNavigationBar(
                selectedPage = selectedPage,
                onSelectLibraryPage = selectTab
            )
        }
    }
}
