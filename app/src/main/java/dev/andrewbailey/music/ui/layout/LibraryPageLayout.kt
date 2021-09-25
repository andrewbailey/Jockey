package dev.andrewbailey.music.ui.layout

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.SwipeableState
import androidx.compose.material.rememberSwipeableState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.google.accompanist.insets.LocalWindowInsets
import dev.andrewbailey.music.ui.layout.CollapsingPageValue.Collapsed
import dev.andrewbailey.music.ui.layout.CollapsingPageValue.Expanded
import dev.andrewbailey.music.ui.library.CollapsedPlayerControls
import dev.andrewbailey.music.ui.navigation.LocalAppNavigator
import dev.andrewbailey.music.ui.player.NowPlayingRoot
import kotlinx.coroutines.launch

@ExperimentalMaterialApi
@Composable
fun LibraryPageLayout(
    modifier: Modifier = Modifier,
    bottomSheetState: SwipeableState<CollapsingPageValue> = rememberSwipeableState(Collapsed),
    bottomBar: @Composable () -> Unit = {},
    content: @Composable () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val bottomInset = with(LocalDensity.current) {
        LocalWindowInsets.current.navigationBars.bottom.toDp()
    }

    with(LocalAppNavigator.current) {
        PopBehavior(
            navigateUp = {
                if (bottomSheetState.currentValue == Expanded) {
                    coroutineScope.launch {
                        bottomSheetState.animateTo(Collapsed)
                    }
                    true
                } else {
                    false
                }
            }
        )
    }

    BottomSheetScaffold(
        modifier = modifier,
        state = bottomSheetState,
        bodyContent = { content() },
        collapsedSheetLayout = {
            Box(
                modifier = Modifier
                    .alpha((1 - 2 * percentExpanded).coerceIn(0f..1f))
                    .topBorder(MaterialTheme.colors.onSurface.copy(alpha = 0.15f), 1.dp)
                    .morphingBackground(
                        color = MaterialTheme.colors.surface,
                        morphHeight = bottomInset,
                        percentVisible = (1 - 6 * percentExpanded).coerceIn(0f..1f)
                    )
                    .padding(bottom = bottomInset)
            ) {
                Column {
                    Surface(color = Color.Transparent) {
                        CollapsedPlayerControls(
                            modifier = Modifier.clickable(
                                onClick = {
                                    coroutineScope.launch {
                                        bottomSheetState.animateTo(Expanded)
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
                percentVisible = percentExpanded
            )
        }
    )
}

private fun Modifier.topBorder(
    color: Color,
    strokeWidth: Dp = Dp.Hairline
) = drawWithContent {
    drawContent()
    drawLine(
        color = color,
        start = Offset(0f, 0f),
        end = Offset(size.width, 0f),
        strokeWidth = strokeWidth.toPx()
    )
}

private fun Modifier.morphingBackground(
    color: Color,
    morphHeight: Dp,
    percentVisible: Float
) = drawBehind {
    drawRect(
        color = color,
        size = Size(
            width = size.width,
            height = size.height - (morphHeight * (1 - percentVisible)).toPx()
        )
    )
}
