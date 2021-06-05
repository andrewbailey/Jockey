package dev.andrewbailey.music.ui.layout

import androidx.annotation.FloatRange
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationResult
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.TwoWayConverter
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.layout.onSizeChanged
import dev.andrewbailey.music.ui.layout.BottomSheetSaveStateKey.Body
import dev.andrewbailey.music.ui.layout.BottomSheetSaveStateKey.BottomSheetContent
import dev.andrewbailey.music.ui.layout.BottomSheetSaveStateKey.CollapsedContent
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

@Composable
fun BottomSheetScaffold(
    bodyContent: @Composable () -> Unit,
    collapsedSheetLayout: @Composable () -> Unit,
    expandedSheetLayout: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    state: CollapsingPageState = rememberCollapsingPageState(CollapsingPageValue.collapsed),
    scrimColor: Color = Color.Black.copy(alpha = 0.6f)
) {
    val coroutineScope = rememberCoroutineScope()
    val collapsedSheetHeight = remember { mutableStateOf(0) }
    val layoutHeight = remember { mutableStateOf(0) }

    val shouldRenderBody = !state.isFullyExpanded
    val expansionPercentage = state.state.value.visibilityPercentage
    val shouldRenderShim = state.isPartiallyExpanded

    val savedState = rememberSaveableStateHolder()

    Layout(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged {
                layoutHeight.value = it.height
            },
        content = {
            if (shouldRenderBody) {
                Box {
                    savedState.SaveableStateProvider(Body) {
                        bodyContent()
                    }
                }
            } else {
                Spacer(modifier = Modifier)
            }

            if (shouldRenderShim) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            // Do nothing -- just make sure tap events can't go through the shim
                        }
                ) {
                    drawRect(scrimColor, alpha = expansionPercentage)
                }
            } else {
                Spacer(modifier = Modifier)
            }

            Surface(
                modifier = Modifier
                    .onSizeChanged {
                        if (state.isFullyCollapsed) {
                            collapsedSheetHeight.value = it.height
                        }
                    }
                    .draggable(
                        orientation = Orientation.Vertical,
                        state = rememberDraggableState { dY ->
                            val distanceToExpandOver =
                                layoutHeight.value - collapsedSheetHeight.value
                            val expansionChange = -dY / distanceToExpandOver
                            val newExpansion = expansionPercentage + expansionChange
                            coroutineScope.launch {
                                state.snapTo(CollapsingPageValue(newExpansion.coerceIn(0.0f, 1.0f)))
                            }
                        },
                        onDragStopped = { velocity ->
                            // TODO: There's a small bug here where the velocity is sometimes zero.
                            //  This causes the bottom sheet to immediately return where it was,
                            //  regardless of the actual swipe speed. This might be fixed in a
                            //  future Compose release. Alternatively, there might be a performance
                            //  issue that's causing too many touch samples to be dropped.
                            coroutineScope.launch {
                                when {
                                    abs(velocity) < 0 -> state.expand()
                                    velocity > 0 -> state.collapse()
                                    expansionPercentage > 0.5 -> state.expand()
                                    else -> state.collapse()
                                }
                            }
                        }
                    )
            ) {
                PartiallyCollapsedPageLayout(
                    collapsedSheetLayout = {
                        with(savedState) {
                            SaveableStateProvider(CollapsedContent) {
                                collapsedSheetLayout()
                            }
                        }
                    },
                    expandedSheetLayout = {
                        with(savedState) {
                            SaveableStateProvider(BottomSheetContent) {
                                expandedSheetLayout()
                            }
                        }
                    },
                    percentExpanded = expansionPercentage,
                    onCollapsedSheetLayoutMeasured = { _, heightPx ->
                        collapsedSheetHeight.value = heightPx
                    }
                )
            }
        },
        measurePolicy = remember(state) {
            MeasurePolicy { measurables, constraints ->
                val bodyLayout = measurables[0]
                val scrimLayout = measurables[1]
                val bottomSheet = measurables[2]

                val bodyPlaceable = bodyLayout.measure(
                    constraints.copy(
                        minHeight = constraints.minHeight - collapsedSheetHeight.value,
                        maxHeight = constraints.maxHeight - collapsedSheetHeight.value
                    )
                )

                val scrimPlaceable = scrimLayout.measure(constraints)
                val bottomSheetPlaceable = bottomSheet.measure(
                    constraints.copy(
                        minHeight = 0
                    )
                )

                layout(constraints.maxWidth, constraints.maxHeight) {
                    bodyPlaceable.place(0, 0)
                    scrimPlaceable.place(0, 0)
                    bottomSheetPlaceable.place(
                        x = 0,
                        y = constraints.maxHeight - bottomSheetPlaceable.height
                    )
                }
            }
        }
    )
}

@Composable
private fun PartiallyCollapsedPageLayout(
    collapsedSheetLayout: @Composable () -> Unit,
    expandedSheetLayout: @Composable () -> Unit,
    percentExpanded: Float,
    modifier: Modifier = Modifier,
    onCollapsedSheetLayoutMeasured: (widthPx: Int, heightPx: Int) -> Unit = { _, _ -> }
) {
    val shouldDrawExpandedLayout = percentExpanded > 0
    val shouldDrawCollapsedLayout = percentExpanded < 1

    Layout(
        modifier = modifier,
        content = {
            if (shouldDrawExpandedLayout) {
                Box(modifier = Modifier.fillMaxHeight()) {
                    expandedSheetLayout()
                }
            }

            if (shouldDrawCollapsedLayout) {
                Box(modifier = Modifier.wrapContentHeight()) {
                    collapsedSheetLayout()
                }
            }
        },
        measurePolicy = remember(percentExpanded) {
            MeasurePolicy { measurables, constraints ->
                val placeableExpandedLayout =
                    measurables.first().takeIf { shouldDrawExpandedLayout }
                        ?.measure(constraints)

                val placeableCollapsedLayout =
                    measurables.last().takeIf { shouldDrawCollapsedLayout }
                        ?.measure(constraints)

                placeableCollapsedLayout?.let {
                    onCollapsedSheetLayoutMeasured(it.width, it.height)
                }

                val collapsedHeight = placeableCollapsedLayout?.height ?: 0
                val height: Int = when (percentExpanded) {
                    0f -> collapsedHeight
                    1f -> constraints.maxHeight
                    in 0f..1f -> {
                        val expansionAmount = constraints.maxHeight - collapsedHeight
                        (percentExpanded * expansionAmount + collapsedHeight).roundToInt()
                    }
                    else -> {
                        throw IllegalArgumentException(
                            "percentExpanded must be within 0..1 (was $percentExpanded)"
                        )
                    }
                }

                layout(constraints.maxWidth, height) {
                    placeableExpandedLayout?.place(0, 0)
                    placeableCollapsedLayout?.place(0, 0)
                }
            }
        }
    )
}

@JvmInline
value class CollapsingPageValue(
    @FloatRange(from = 0.0, to = 1.0)
    val visibilityPercentage: Float
) {

    companion object {
        val expanded = CollapsingPageValue(1.0f)
        val collapsed = CollapsingPageValue(0.0f)
    }

}

@Composable
fun rememberCollapsingPageState(
    initialValue: CollapsingPageValue
): CollapsingPageState {
    return rememberSaveable(
        saver = CollapsingPageState.Saver()
    ) {
        CollapsingPageState(initialValue)
    }
}

class CollapsingPageState(
    initialValue: CollapsingPageValue
) {

    private val animator = Animatable(
        initialValue = initialValue,
        typeConverter = object : TwoWayConverter<CollapsingPageValue, AnimationVector1D> {
            override val convertFromVector: (AnimationVector1D) -> CollapsingPageValue = {
                CollapsingPageValue(it.value)
            }

            override val convertToVector: (CollapsingPageValue) -> AnimationVector1D = {
                AnimationVector1D(it.visibilityPercentage)
            }
        }
    ).apply {
        updateBounds(
            lowerBound = CollapsingPageValue.collapsed,
            upperBound = CollapsingPageValue.expanded
        )
    }

    val state = animator.asState()

    val currentValue
        get() = animator.asState().value

    val isFullyExpanded: Boolean
        get() = currentValue == CollapsingPageValue.expanded

    val isFullyCollapsed: Boolean
        get() = currentValue == CollapsingPageValue.collapsed

    val isPartiallyExpanded: Boolean
        get() = !isFullyExpanded && !isFullyCollapsed

    private suspend fun animateTo(
        targetValue: CollapsingPageValue,
        anim: AnimationSpec<CollapsingPageValue> = SpringSpec(
            stiffness = 1000f
        )
    ): AnimationResult<CollapsingPageValue, AnimationVector1D> {
        return animator.animateTo(
            targetValue = targetValue,
            animationSpec = anim
        )
    }

    suspend fun snapTo(targetValue: CollapsingPageValue) {
        animator.snapTo(targetValue)
    }

    suspend fun expand(): Boolean {
        val animationResult = animateTo(CollapsingPageValue.expanded)
        return animationResult.endState.value == CollapsingPageValue.expanded
    }

    suspend fun collapse(): Boolean {
        val animationResult = animateTo(CollapsingPageValue.collapsed)
        return animationResult.endState.value == CollapsingPageValue.expanded
    }

    companion object {
        fun Saver() = Saver<CollapsingPageState, Float>(
            save = { it.currentValue.visibilityPercentage },
            restore = { CollapsingPageState(CollapsingPageValue(it)) }
        )
    }
}

private enum class BottomSheetSaveStateKey {
    Body,
    CollapsedContent,
    BottomSheetContent
}
