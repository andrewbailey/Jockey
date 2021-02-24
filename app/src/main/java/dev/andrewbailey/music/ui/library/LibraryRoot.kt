package dev.andrewbailey.music.ui.library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import dev.andrewbailey.music.R
import dev.andrewbailey.music.ui.layout.BottomSheetScaffold
import dev.andrewbailey.music.ui.layout.CollapsingPageValue
import dev.andrewbailey.music.ui.layout.rememberCollapsingPageState
import dev.andrewbailey.music.ui.library.songs.AllSongsRoot
import dev.andrewbailey.music.ui.navigation.LibraryPage
import dev.andrewbailey.music.ui.navigation.LocalAppNavigator
import dev.andrewbailey.music.ui.player.NowPlayingRoot
import kotlinx.coroutines.launch

@Composable
fun LibraryRoot(
    modifier: Modifier = Modifier
) {
    // TODO: Use `rememberSaveable` after this bug is resolved in Compose:
    //  https://issuetracker.google.com/issues/180042685
    val selectedPage = /*rememberSaveable { */mutableStateOf(LibraryPage.Songs)/* }*/
    val coroutineScope = rememberCoroutineScope()

    val bottomSheetState = rememberCollapsingPageState(CollapsingPageValue.collapsed)
    val visibilityPercentage = bottomSheetState.state.value.visibilityPercentage

    LocalAppNavigator.current.overridePopBehavior(
        navigateUp = {
            if (bottomSheetState.isFullyExpanded) {
                coroutineScope.launch {
                    bottomSheetState.collapse()
                }
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
        collapsedSheetLayout = {
            LibraryNavigation(
                selectedPage = selectedPage.value,
                modifier = Modifier
                    .alpha((1 - 2 * visibilityPercentage).coerceIn(0f..1f)),
                showFullPlaybackControls = {
                    coroutineScope.launch {
                        bottomSheetState.expand()
                    }
                },
                selectTab = { newPage ->
                    selectedPage.value = newPage
                }
            )
        },
        expandedSheetLayout = {
            NowPlayingRoot(
                percentVisible = visibilityPercentage
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
