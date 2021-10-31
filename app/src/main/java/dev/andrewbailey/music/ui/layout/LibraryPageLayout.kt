package dev.andrewbailey.music.ui.layout

import androidx.compose.foundation.clickable
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.SwipeableState
import androidx.compose.material.rememberSwipeableState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.google.accompanist.insets.LocalWindowInsets
import dev.andrewbailey.encore.player.state.MediaPlayerState.Prepared
import dev.andrewbailey.music.ui.data.LocalPlaybackController
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
    bottomBar: @Composable (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val playbackController = LocalPlaybackController.current
    val isMediaPlaying = playbackController.playbackState.collectAsState(null).value is Prepared

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
        expandable = isMediaPlaying,
        bodyContent = { content() },
        collapsedSheetLayout = {
            CollapsedPlayerControls(
                additionalContent = bottomBar,
                onClickBar = {
                    coroutineScope.launch {
                        bottomSheetState.animateTo(Expanded)
                    }
                }
            )
        },
        expandedSheetLayout = {
            NowPlayingRoot(
                percentVisible = percentExpanded
            )
        }
    )
}

@Composable
private fun BottomSheetScaffoldScope.CollapsedPlayerControls(
    modifier: Modifier = Modifier,
    additionalContent: @Composable (() -> Unit)? = null,
    onClickBar: () -> Unit
) {
    val bottomInsetPx = LocalWindowInsets.current.navigationBars.bottom
    val bottomInsetDp = with(LocalDensity.current) { bottomInsetPx.toDp() }

    Layout(
        modifier = modifier
            .alpha((1 - 2 * percentExpanded).coerceIn(0f..1f))
            .topBorder(MaterialTheme.colors.onSurface.copy(alpha = 0.15f), 1.dp)
            .morphingBackground(
                color = MaterialTheme.colors.surface,
                morphHeight = bottomInsetDp,
                percentVisible = (1 - 6 * percentExpanded).coerceIn(0f..1f)
            ),
        content = {
            Surface(color = Color.Transparent) {
                CollapsedPlayerControls(
                    modifier = Modifier.clickable(
                        onClick = onClickBar
                    )
                )
            }

            if (additionalContent != null) {
                Surface(color = Color.Transparent) {
                    additionalContent()
                }
            }
        },
        measurePolicy = { measurables, constraints ->
            val playbackControls = measurables[0].measure(constraints)
            val additionalUi = measurables.getOrNull(1)
                ?.measure(
                    constraints.copy(
                        maxHeight = constraints.maxHeight - playbackControls.measuredHeight
                    )
                )

            val contentHeight = playbackControls.measuredHeight +
                (additionalUi?.measuredHeight ?: 0)

            if (contentHeight == 0) {
                layout(0, 0) {}
            } else {
                layout(constraints.maxWidth, contentHeight + bottomInsetPx) {
                    playbackControls.place(0, 0)
                    additionalUi?.place(0, playbackControls.measuredHeight)
                }
            }
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