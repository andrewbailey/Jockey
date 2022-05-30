package dev.andrewbailey.music.ui.layout

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.material.LocalElevationOverlay
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import dev.andrewbailey.encore.player.state.MediaPlayerState.Prepared
import dev.andrewbailey.music.ui.data.LocalPlaybackController
import dev.andrewbailey.music.ui.layout.ModalStateValue.Collapsed
import dev.andrewbailey.music.ui.layout.ModalStateValue.Expanded
import dev.andrewbailey.music.ui.library.CollapsedPlayerControls
import dev.andrewbailey.music.ui.navigation.LocalAppNavigator
import dev.andrewbailey.music.ui.player.NowPlayingRoot
import dev.andrewbailey.music.util.collectAsState
import dev.andrewbailey.music.util.plus
import kotlinx.coroutines.launch

@Composable
fun LibraryPageLayout(
    modifier: Modifier = Modifier,
    nowPlayingModalState: ModalState = rememberModalState(Collapsed),
    bottomBar: @Composable (() -> Unit)? = null,
    content: @Composable (PaddingValues) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val playbackController = LocalPlaybackController.current
    val isMediaPlaying = playbackController.playbackState.collectAsState().value is Prepared

    with(LocalAppNavigator.current) {
        PopBehavior(
            navigateUp = {
                if (nowPlayingModalState.currentValue == Expanded) {
                    coroutineScope.launch {
                        nowPlayingModalState.collapse()
                    }
                    true
                } else {
                    false
                }
            }
        )
    }

    val systemUiController = rememberSystemUiController()
    val isLightTheme = MaterialTheme.colors.isLight
    val isNowPlayingCollapsed = nowPlayingModalState.percentExpanded < 0.5f

    SideEffect {
        systemUiController.setStatusBarColor(
            color = Color.Transparent,
            darkIcons = isLightTheme && isNowPlayingCollapsed
        )

        systemUiController.setNavigationBarColor(
            color = Color.Transparent,
            darkIcons = isLightTheme
        )
    }

    ModalScaffold(
        modifier = modifier,
        state = nowPlayingModalState,
        expandable = isMediaPlaying,
        bodyContent = { paddingValues ->
            content(paddingValues)
        },
        collapsedSheetLayout = {
            CollapsedPlayerControls(
                nowPlayingModalState = nowPlayingModalState,
                additionalContent = bottomBar,
                onClickBar = {
                    coroutineScope.launch {
                        nowPlayingModalState.expand()
                    }
                }
            )
        },
        expandedSheetLayout = {
            NowPlayingRoot(
                percentVisible = nowPlayingModalState.percentExpanded,
                modifier = Modifier.fillMaxSize()
            )
        }
    )
}

@Composable
private fun CollapsedPlayerControls(
    nowPlayingModalState: ModalState,
    modifier: Modifier = Modifier,
    additionalContent: @Composable (() -> Unit)? = null,
    onClickBar: () -> Unit
) {
    val density = LocalDensity.current
    val insets = WindowInsets.safeContent.asPaddingValues()

    val surfaceColor = LocalElevationOverlay.current?.apply(
        color = MaterialTheme.colors.surface,
        elevation = 4.dp
    ) ?: MaterialTheme.colors.surface

    Layout(
        modifier = modifier
            .alpha((1 - 2 * nowPlayingModalState.percentExpanded).coerceIn(0f..1f))
            .topBorder(MaterialTheme.colors.onSurface.copy(alpha = 0.15f), 1.dp)
            .morphingBackground(
                color = surfaceColor,
                morphHeight = insets.calculateBottomPadding(),
                percentVisible = (1 - 6 * nowPlayingModalState.percentExpanded).coerceIn(0f..1f)
            ),
        content = {
            Box(
                modifier = Modifier
                    .clipToBounds()
                    .padding(
                        start = insets.calculateStartPadding(LocalLayoutDirection.current),
                        end = insets.calculateEndPadding(LocalLayoutDirection.current),
                    )
            ) {
                CollapsedPlayerControls(
                    modifier = Modifier.clickable(
                        onClick = onClickBar
                    )
                )
            }

            if (additionalContent != null) {
                Box(
                    modifier = Modifier
                        .clipToBounds()
                        .padding(
                            start = insets.calculateStartPadding(LocalLayoutDirection.current),
                            end = insets.calculateEndPadding(LocalLayoutDirection.current),
                        )
                ) {
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
                val bottomPaddingPx = with(density) { insets.calculateBottomPadding().roundToPx() }
                layout(constraints.maxWidth, contentHeight + bottomPaddingPx) {
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
