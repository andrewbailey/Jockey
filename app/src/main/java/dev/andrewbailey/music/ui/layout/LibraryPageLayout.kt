package dev.andrewbailey.music.ui.layout

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import com.google.accompanist.insets.LocalWindowInsets
import dev.andrewbailey.music.ui.layout.CollapsingPageValue.Companion.collapsed
import dev.andrewbailey.music.ui.library.CollapsedPlayerControls
import dev.andrewbailey.music.ui.navigation.LocalAppNavigator
import dev.andrewbailey.music.ui.player.NowPlayingRoot
import kotlinx.coroutines.launch

@Composable
fun LibraryPageLayout(
    modifier: Modifier = Modifier,
    bottomSheetState: CollapsingPageState = rememberCollapsingPageState(collapsed),
    bottomBar: @Composable () -> Unit = {},
    content: @Composable () -> Unit
) {
    val visibilityPercentage = bottomSheetState.state.value.visibilityPercentage
    val coroutineScope = rememberCoroutineScope()

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
        bodyContent = content,
        collapsedSheetLayout = {
            Surface(
                color = Color.Transparent,
                modifier = Modifier
                    .alpha((1 - 2 * visibilityPercentage).coerceIn(0f..1f))
                    .background(MaterialTheme.colors.surface)
                    .padding(
                        bottom = with(LocalDensity.current) {
                            LocalWindowInsets.current.navigationBars.bottom.toDp() *
                                (1 - 6 * visibilityPercentage).coerceIn(0f..1f)
                        }
                    )
            ) {
                Column {
                    Surface(color = Color.Transparent) {
                        CollapsedPlayerControls(
                            modifier = Modifier.clickable(
                                onClick = {
                                    coroutineScope.launch {
                                        bottomSheetState.expand()
                                    }
                                }
                            )
                        )
                    }

                    Surface(color = Color.Transparent) {
                        bottomBar()
                    }
                }
            }
        },
        expandedSheetLayout = {
            NowPlayingRoot(
                percentVisible = visibilityPercentage
            )
        }
    )
}
