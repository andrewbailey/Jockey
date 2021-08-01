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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import dev.andrewbailey.music.util.subcomposeSingle
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
    SubcomposeLayout(modifier) { constraints ->
        val wrapContentSizeConstraints = constraints.copy(minWidth = 0, minHeight = 0)

        val scrim = subcomposeSingle("scrim") {
            Scrim(
                state = state,
                color = scrimColor,
                modifier = Modifier.fillMaxSize()
            )
        }.measure(constraints)

        val collapsedSheetHeight = subcomposeSingle("collapsedSheetContents") {
            collapsedSheetLayout()
        }.measure(wrapContentSizeConstraints).height

        val layoutSize = IntSize(scrim.width, scrim.height)
        layout(layoutSize.width, layoutSize.height) {
            subcomposeSingle("body") {
                BodyContent(
                    bodyContent = bodyContent
                )
            }.measure(
                constraints.copy(
                    minHeight = 0,
                    maxHeight = constraints.maxHeight - collapsedSheetHeight
                )
            ).place(0, 0)

            scrim.place(0, 0)

            subcomposeSingle("sheet") {
                CollapsableContent(
                    state = state,
                    collapsedContent = collapsedSheetLayout,
                    expandedContent = expandedSheetLayout,
                    modifier = Modifier.collapsible(
                        state = state,
                        distanceToExpandOver = constraints.maxHeight - collapsedSheetHeight
                    )
                )
            }.measure(wrapContentSizeConstraints).run {
                place(0, layoutSize.height - height)
            }
        }
    }
}

private fun Modifier.collapsible(
    state: CollapsingPageState,
    distanceToExpandOver: Int,
    enabled: Boolean = true,
    reverseDirection: Boolean = false,
): Modifier = composed {
    val coroutineScope = rememberCoroutineScope()

    draggable(
        orientation = Orientation.Vertical,
        enabled = enabled,
        reverseDirection = reverseDirection,
        startDragImmediately = state.isAnimationRunning,
        state = rememberDraggableState { dY ->
            val expansionChange = -dY / distanceToExpandOver
            val newExpansion = state.currentValue.visibilityPercentage + expansionChange
            coroutineScope.launch {
                state.snapTo(CollapsingPageValue(newExpansion.coerceIn(0.0f, 1.0f)))
            }
        },
        onDragStopped = { velocity -> launch { state.performFling(velocity) } }
    )
}

@Composable
private fun BodyContent(
    modifier: Modifier = Modifier,
    bodyContent: @Composable () -> Unit = {}
) {
    val shouldRenderBody = true // !state.isFullyExpanded
    if (shouldRenderBody) {
        Box(modifier = modifier) {
            bodyContent()
        }
    } else {
        Spacer(modifier = modifier)
    }
}

@Composable
private fun Scrim(
    state: CollapsingPageState,
    color: Color,
    modifier: Modifier = Modifier
) {
    val shouldRenderShim = state.isPartiallyExpanded
    if (shouldRenderShim) {
        Canvas(
            modifier = modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    // Do nothing -- just make sure tap events can't go through the shim
                }
        ) {
            drawRect(color, alpha = state.currentValue.visibilityPercentage)
        }
    } else {
        Spacer(modifier = modifier)
    }
}

@Composable
private fun CollapsableContent(
    state: CollapsingPageState,
    modifier: Modifier = Modifier,
    collapsedContent: @Composable () -> Unit = {},
    expandedContent: @Composable () -> Unit = {}
) {
    val expansionPercentage = state.currentValue.visibilityPercentage

    Surface(
        modifier = modifier
            .wrapContentHeight()
            .shadow(elevation = 16.dp)
    ) {
        PartiallyCollapsedPageLayout(
            collapsedSheetLayout = {
                collapsedContent()
            },
            expandedSheetLayout = {
                expandedContent()
            },
            percentExpanded = expansionPercentage
        )
    }
}

@Composable
private fun PartiallyCollapsedPageLayout(
    collapsedSheetLayout: @Composable () -> Unit,
    expandedSheetLayout: @Composable () -> Unit,
    percentExpanded: Float,
    modifier: Modifier = Modifier
) {
    val shouldDrawExpandedLayout = true
    val shouldDrawCollapsedLayout = true

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
                val (expandedContent, collapsedContent) = measurables
                    .map { it.measure(constraints) }

                val collapsedHeight = collapsedContent.height
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
                }.coerceIn(constraints.minHeight, constraints.maxHeight)

                layout(constraints.maxWidth, height) {
                    expandedContent.takeIf { shouldDrawExpandedLayout }?.place(0, 0)
                    collapsedContent.takeIf { shouldDrawCollapsedLayout }?.place(0, 0)
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

    val isAnimationRunning: Boolean
        get() = animator.isRunning

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

    suspend fun performFling(velocity: Float) {
        animateTo(
            targetValue = when {
                velocity < 0 -> CollapsingPageValue.expanded
                velocity > 0 -> CollapsingPageValue.collapsed
                state.value.visibilityPercentage < 0.5 -> CollapsingPageValue.expanded
                else -> CollapsingPageValue.collapsed
            }
        )
    }

    companion object {
        fun Saver() = Saver<CollapsingPageState, Float>(
            save = { it.currentValue.visibilityPercentage },
            restore = { CollapsingPageState(CollapsingPageValue(it)) }
        )
    }
}
