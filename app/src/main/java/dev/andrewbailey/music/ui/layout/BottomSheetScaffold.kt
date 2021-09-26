@file:OptIn(ExperimentalMaterialApi::class)
package dev.andrewbailey.music.ui.layout

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.Orientation.Vertical
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import dev.andrewbailey.music.ui.layout.CollapsingPageValue.Collapsed
import dev.andrewbailey.music.ui.layout.CollapsingPageValue.Expanded
import dev.andrewbailey.music.util.ConsumeWindowInsets
import dev.andrewbailey.music.util.subcomposeSingle
import kotlin.math.roundToInt

@Composable
fun BottomSheetScaffold(
    bodyContent: @Composable BottomSheetScaffoldScope.() -> Unit,
    collapsedSheetLayout: @Composable BottomSheetScaffoldScope.() -> Unit,
    expandedSheetLayout: @Composable BottomSheetScaffoldScope.() -> Unit,
    modifier: Modifier = Modifier,
    state: SwipeableState<CollapsingPageValue> = rememberSwipeableState(Collapsed),
    scrimColor: Color = Color.Black.copy(alpha = 0.6f)
) {
    with(
        BottomSheetScaffoldScope(
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
    ) {
        SubcomposeLayout(modifier) { constraints ->
            val layoutSize = IntSize(constraints.maxWidth, constraints.maxHeight)
            val wrapContentSizeConstraints = constraints.copy(minWidth = 0, minHeight = 0)

            val collapsedSheetHeight = subcomposeSingle("collapsedSheetContents") {
                collapsedSheetLayout()
            }.measure(wrapContentSizeConstraints).height

            val distanceToExpandOver = layoutSize.height - collapsedSheetHeight

            layout(layoutSize.width, layoutSize.height) {
                subcomposeSingle("body") {
                    ConsumeWindowInsets(bottomPx = collapsedSheetHeight) {
                        bodyContent()
                    }
                }.measure(
                    constraints.copy(
                        minHeight = 0,
                        maxHeight = constraints.maxHeight - collapsedSheetHeight
                    )
                ).place(0, 0)

                subcomposeSingle("scrim") {
                    Scrim(
                        color = scrimColor,
                        modifier = Modifier.fillMaxSize()
                    )
                }.measure(constraints).place(0, 0)

                subcomposeSingle("sheet") {
                    CollapsableContent(
                        collapsedContent = collapsedSheetLayout,
                        expandedContent = expandedSheetLayout,
                        modifier = Modifier.swipeable(
                            state = state,
                            anchors = mapOf(
                                0f to Collapsed,
                                -distanceToExpandOver.toFloat() to Expanded
                            ),
                            orientation = Vertical,
                            resistance = null
                        )
                    )
                }.measure(constraints).place(
                    x = 0,
                    y = (distanceToExpandOver * (1 - percentExpanded)).roundToInt()
                )
            }
        }
    }
}

@Composable
private fun BottomSheetScaffoldScope.Scrim(
    color: Color,
    modifier: Modifier = Modifier
) {
    val shouldRenderShim = percentExpanded > 0 && percentExpanded < 1
    if (shouldRenderShim) {
        Canvas(
            modifier = modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    // Do nothing -- just make sure tap events can't go through the shim
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
        Box(Modifier.fillMaxHeight()) {
            collapsedContent()
        }
    }
}

@JvmInline
value class BottomSheetScaffoldScope(
    val percentExpanded: Float
)

enum class CollapsingPageValue {
    Expanded,
    Collapsed
}
