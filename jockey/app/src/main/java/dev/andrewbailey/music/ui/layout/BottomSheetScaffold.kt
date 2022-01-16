@file:OptIn(ExperimentalMaterialApi::class)
package dev.andrewbailey.music.ui.layout

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.Orientation.Vertical
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Surface
import androidx.compose.material.SwipeableState
import androidx.compose.material.rememberSwipeableState
import androidx.compose.material.swipeable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isSpecified
import dev.andrewbailey.music.ui.layout.CollapsingPageValue.Collapsed
import dev.andrewbailey.music.ui.layout.CollapsingPageValue.Expanded
import dev.andrewbailey.music.util.ConsumeWindowInsets
import dev.andrewbailey.music.util.subcomposeSingle
import kotlin.math.roundToInt
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

@Composable
fun BottomSheetScaffold(
    bodyContent: @Composable BottomSheetScaffoldScope.() -> Unit,
    collapsedSheetLayout: @Composable BottomSheetScaffoldScope.() -> Unit,
    expandedSheetLayout: @Composable BottomSheetScaffoldScope.() -> Unit,
    modifier: Modifier = Modifier,
    state: SwipeableState<CollapsingPageValue> = rememberSwipeableState(Collapsed),
    expandable: Boolean = true,
    scrimColor: Color = Color.Black.copy(alpha = 0.6f)
) {
    SubcomposeLayout(modifier) { constraints ->
        val layoutSize = IntSize(constraints.maxWidth, constraints.maxHeight)
        val wrapContentSizeConstraints = constraints.copy(minWidth = 0, minHeight = 0)

        val collapsedSheetHeight = subcomposeSingle("collapsedSheetContents") {
            with(BottomSheetScaffoldScope(state)) {
                collapsedSheetLayout()
            }
        }.measure(wrapContentSizeConstraints).height

        layout(layoutSize.width, layoutSize.height) {
            subcomposeSingle("collapsingPage") {
                BottomSheetScaffold(
                    bodyContent = bodyContent,
                    sheetContent = {
                        CollapsableContent(
                            collapsedContent = collapsedSheetLayout,
                            expandedContent = expandedSheetLayout
                        )
                    },
                    collapsedSheetHeightPx = collapsedSheetHeight,
                    state = state,
                    expandable = expandable,
                    scrimColor = scrimColor
                )
            }.measure(wrapContentSizeConstraints).place(0, 0)
        }
    }
}

@Composable
fun BottomSheetScaffold(
    bodyContent: @Composable BottomSheetScaffoldScope.() -> Unit,
    sheetContent: @Composable BottomSheetScaffoldScope.() -> Unit,
    collapsedSheetHeightPx: Int,
    modifier: Modifier = Modifier,
    state: SwipeableState<CollapsingPageValue> = rememberSwipeableState(Collapsed),
    maximumExpandedHeight: Dp = Dp.Unspecified,
    expandable: Boolean = true,
    scrimColor: Color = Color.Black.copy(alpha = 0.6f)
) {
    with(BottomSheetScaffoldScope(state)) {
        val localDensity = LocalDensity.current
        val coroutineScope = rememberCoroutineScope()

        SubcomposeLayout(modifier) { constraints ->
            val layoutSize = IntSize(constraints.maxWidth, constraints.maxHeight)

            val maximumExpandedHeightPx = with(localDensity) {
                maximumExpandedHeight.takeIf { it.isSpecified }?.roundToPx()
            }?.coerceIn(0..layoutSize.height) ?: layoutSize.height
            val expandedTopMargin = layoutSize.height - maximumExpandedHeightPx
            val distanceToExpandOver = maximumExpandedHeightPx - collapsedSheetHeightPx

            layout(layoutSize.width, layoutSize.height) {
                subcomposeSingle("body") {
                    ConsumeWindowInsets(bottomPx = collapsedSheetHeightPx) {
                        bodyContent()
                    }
                }.measure(
                    constraints.copy(
                        minHeight = 0,
                        maxHeight = constraints.maxHeight - collapsedSheetHeightPx
                    )
                ).place(0, 0)

                subcomposeSingle("scrim") {
                    Scrim(
                        color = scrimColor,
                        showWhenExpanded = expandedTopMargin > 0,
                        onDismissSheet = {
                            coroutineScope.launch {
                                state.animateTo(Collapsed)
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }.measure(constraints).place(0, 0)

                subcomposeSingle("sheet") {
                    Box(
                        modifier = Modifier.swipeable(
                            state = state,
                            anchors = mapOf(
                                0f to Collapsed,
                                -distanceToExpandOver.toFloat() to Expanded
                            ),
                            enabled = expandable || state.currentValue == Expanded,
                            orientation = Vertical,
                            resistance = null
                        )
                    ) {
                        sheetContent()
                    }
                }.measure(
                    constraints.copy(
                        minHeight = 0,
                        maxHeight = maximumExpandedHeightPx
                    )
                ).place(
                    x = 0,
                    y = (distanceToExpandOver * (1 - percentExpanded) + expandedTopMargin)
                        .roundToInt()
                )
            }
        }
    }
}

@Composable
private fun BottomSheetScaffoldScope.Scrim(
    color: Color,
    showWhenExpanded: Boolean,
    onDismissSheet: () -> Unit,
    modifier: Modifier = Modifier
) {
    val partiallyExpanded = percentExpanded > 0 && percentExpanded < 1
    val fullyExpanded = percentExpanded == 1f
    if (partiallyExpanded || (showWhenExpanded && fullyExpanded)) {
        Canvas(
            modifier = modifier
                .fillMaxSize()
                .pointerInput(fullyExpanded) {
                    // If we're not fully expanded, do nothing. This ensures that tap events
                    // can't go through the shim. If we are expanded, use any interactions on the
                    // shim to collapse the bottom sheet.
                    if (fullyExpanded) {
                        coroutineScope {
                            launch {
                                detectDragGestures { _, _ ->
                                    onDismissSheet()
                                }
                            }

                            launch {
                                detectTapGestures {
                                    onDismissSheet()
                                }
                            }
                        }
                    }
                }
        ) {
            drawRect(color, alpha = percentExpanded)
        }
    } else {
        Spacer(modifier = modifier)
    }
}

@Composable
private fun BottomSheetScaffoldScope.CollapsableContent(
    modifier: Modifier = Modifier,
    collapsedContent: @Composable BottomSheetScaffoldScope.() -> Unit = {},
    expandedContent: @Composable BottomSheetScaffoldScope.() -> Unit = {}
) {
    Surface(
        modifier = modifier
            .shadow(elevation = 16.dp)
    ) {
        Box(Modifier.wrapContentHeight()) {
            expandedContent()
        }
        if (percentExpanded < 1) {
            Box(Modifier.fillMaxHeight()) {
                collapsedContent()
            }
        }
    }
}

@JvmInline
value class BottomSheetScaffoldScope(
    val percentExpanded: Float
) {
    constructor(
        state: SwipeableState<CollapsingPageValue>
    ) : this(
        percentExpanded = with(state.progress) {
            if (from == to) when (from) {
                Expanded -> 1f
                Collapsed -> 0f
            } else when (from) {
                Expanded -> 1 - fraction
                Collapsed -> fraction
            }
        }
    )
}

enum class CollapsingPageValue {
    Expanded,
    Collapsed
}
