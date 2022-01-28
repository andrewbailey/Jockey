@file:OptIn(ExperimentalMaterialApi::class)
package dev.andrewbailey.music.ui.layout

import androidx.compose.animation.core.AnimationSpec
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
import androidx.compose.material.SwipeableDefaults
import androidx.compose.material.SwipeableState
import androidx.compose.material.swipeable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import dev.andrewbailey.music.ui.layout.ModalState.Companion.modalSheet
import dev.andrewbailey.music.ui.layout.ModalStateValue.Collapsed
import dev.andrewbailey.music.ui.layout.ModalStateValue.Expanded
import dev.andrewbailey.music.util.ConsumeWindowInsets
import dev.andrewbailey.music.util.subcomposeSingle
import kotlin.math.roundToInt
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

enum class ModalStateValue {
    Expanded,
    Collapsed
}

class ModalState(
    initialValue: ModalStateValue,
    animationSpec: AnimationSpec<Float>,
    confirmStateChange: (newValue: ModalStateValue) -> Boolean
) {

    var confirmedState by mutableStateOf(initialValue)
        private set

    private val swipeableState = SwipeableState(
        initialValue = initialValue,
        animationSpec = animationSpec,
        confirmStateChange = {
            if (confirmStateChange(it)) {
                confirmedState = it
                true
            } else {
                false
            }
        }
    )

    val currentValue: ModalStateValue get() =
        swipeableState.currentValue

    val targetValue: ModalStateValue get() =
        swipeableState.targetValue

    val percentExpanded: Float
        get() = with(swipeableState.progress) {
            if (from == to) when (from) {
                Expanded -> 1f
                Collapsed -> 0f
            } else when (from) {
                Expanded -> 1 - fraction
                Collapsed -> fraction
            }
        }

    suspend fun expand() {
        confirmedState = Expanded
        swipeableState.animateTo(Expanded)
    }

    suspend fun collapse() {
        confirmedState = Collapsed
        swipeableState.animateTo(Collapsed)
    }

    companion object {
        fun Saver(
            animationSpec: AnimationSpec<Float>,
            confirmStateChange: (ModalStateValue) -> Boolean
        ) = Saver<ModalState, ModalStateValue>(
            save = { it.currentValue },
            restore = { ModalState(it, animationSpec, confirmStateChange) }
        )

        fun Modifier.modalSheet(
            state: ModalState,
            distanceToExpandOver: Float,
            enabled: Boolean = true
        ): Modifier = swipeable(
            state = state.swipeableState,
            anchors = mapOf(
                0f to Collapsed,
                -distanceToExpandOver to Expanded
            ),
            enabled = enabled,
            orientation = Vertical,
            resistance = null
        )
    }
}

@Composable
fun rememberModalState(
    initialValue: ModalStateValue,
    animationSpec: AnimationSpec<Float> = SwipeableDefaults.AnimationSpec,
    confirmStateChange: (newValue: ModalStateValue) -> Boolean = { true }
): ModalState {
    return rememberSaveable(
        saver = ModalState.Saver(
            animationSpec = animationSpec,
            confirmStateChange = confirmStateChange
        )
    ) {
        ModalState(
            initialValue = initialValue,
            animationSpec = animationSpec,
            confirmStateChange = confirmStateChange
        )
    }
}

@Composable
fun ModalScaffold(
    bodyContent: @Composable () -> Unit,
    collapsedSheetLayout: @Composable () -> Unit,
    expandedSheetLayout: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    state: ModalState = rememberModalState(Collapsed),
    expandable: Boolean = true,
    scrimColor: Color = Color.Black.copy(alpha = 0.6f)
) {
    SubcomposeLayout(modifier) { constraints ->
        val layoutSize = IntSize(constraints.maxWidth, constraints.maxHeight)
        val wrapContentSizeConstraints = constraints.copy(minWidth = 0, minHeight = 0)

        val collapsedSheetHeight = subcomposeSingle("collapsedSheetContents") {
            collapsedSheetLayout()
        }.measure(wrapContentSizeConstraints).height

        layout(layoutSize.width, layoutSize.height) {
            subcomposeSingle("collapsingPage") {
                ModalScaffold(
                    bodyContent = bodyContent,
                    sheetContent = {
                        CollapsableContent(
                            state = state,
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
fun ModalScaffold(
    bodyContent: @Composable () -> Unit,
    sheetContent: @Composable () -> Unit,
    collapsedSheetHeightPx: Int,
    modifier: Modifier = Modifier,
    state: ModalState = rememberModalState(Collapsed),
    maximumExpandedHeight: Dp = Dp.Unspecified,
    expandable: Boolean = true,
    scrimColor: Color = Color.Black.copy(alpha = 0.6f)
) {
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
                    state = state,
                    color = scrimColor,
                    showWhenExpanded = expandedTopMargin > 0,
                    onDismissSheet = {
                        coroutineScope.launch {
                            state.collapse()
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }.measure(constraints).place(0, 0)

            subcomposeSingle("sheet") {
                Box(
                    modifier = Modifier.modalSheet(
                        state = state,
                        distanceToExpandOver = distanceToExpandOver.toFloat(),
                        enabled = expandable || state.currentValue == Expanded,
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
                y = (distanceToExpandOver * (1 - state.percentExpanded) + expandedTopMargin)
                    .roundToInt()
            )
        }
    }
}

@Composable
private fun Scrim(
    state: ModalState,
    color: Color,
    showWhenExpanded: Boolean,
    onDismissSheet: () -> Unit,
    modifier: Modifier = Modifier
) {
    val partiallyExpanded = state.percentExpanded > 0 && state.percentExpanded < 1
    val fullyExpanded = state.percentExpanded == 1f
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
            drawRect(color, alpha = state.percentExpanded)
        }
    } else {
        Spacer(modifier = modifier)
    }
}

@Composable
private fun CollapsableContent(
    state: ModalState,
    modifier: Modifier = Modifier,
    collapsedContent: @Composable () -> Unit = {},
    expandedContent: @Composable () -> Unit = {}
) {
    Surface(
        modifier = modifier
            .shadow(elevation = 16.dp)
    ) {
        Box(Modifier.wrapContentHeight()) {
            expandedContent()
        }
        if (state.percentExpanded < 1) {
            Box(Modifier.fillMaxHeight()) {
                collapsedContent()
            }
        }
    }
}
