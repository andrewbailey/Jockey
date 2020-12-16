package dev.andrewbailey.music.ui.layout

import androidx.annotation.FloatRange
import androidx.compose.animation.asDisposableClock
import androidx.compose.animation.core.AnimatedFloat
import androidx.compose.animation.core.AnimationClockObservable
import androidx.compose.animation.core.AnimationEndReason
import androidx.compose.animation.core.AnimationEndReason.Interrupted
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.SpringSpec
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.savedinstancestate.ExperimentalRestorableStateHolder
import androidx.compose.runtime.savedinstancestate.RestorableStateHolder
import androidx.compose.runtime.savedinstancestate.Saver
import androidx.compose.runtime.savedinstancestate.rememberRestorableStateHolder
import androidx.compose.runtime.savedinstancestate.rememberSavedInstanceState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.gesture.scrollorientationlocking.Orientation
import androidx.compose.ui.gesture.tapGestureFilter
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.MeasuringIntrinsicsMeasureBlocks
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.node.ExperimentalLayoutNodeApi
import androidx.compose.ui.platform.AmbientAnimationClock
import dev.andrewbailey.music.ui.layout.BottomSheetSaveStateKey.Body
import dev.andrewbailey.music.ui.layout.BottomSheetSaveStateKey.BottomSheetContent
import dev.andrewbailey.music.ui.layout.BottomSheetSaveStateKey.CollapsedContent
import kotlin.math.roundToInt

@OptIn(ExperimentalRestorableStateHolder::class, ExperimentalLayoutNodeApi::class)
@Composable
fun BottomSheetScaffold(
    bodyContent: @Composable () -> Unit,
    collapsedSheetLayout: @Composable () -> Unit,
    expandedSheetLayout: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    state: CollapsingPageState = rememberCollapsingPageState(CollapsingPageValue.collapsed),
    onStateChanged: ((CollapsingPageValue) -> Unit)? = null,
    scrimColor: Color = Color.Black.copy(alpha = 0.6f)
) {
    val collapsedSheetHeight = remember { mutableStateOf(0) }
    val layoutHeight = remember { mutableStateOf(0) }

    val shouldRenderBody = !state.isFullyExpanded
    val shouldRenderShim = state.isPartiallyExpanded

    val savedState = rememberRestorableStateHolder<BottomSheetSaveStateKey>()

    Layout(
        modifier = modifier.fillMaxSize()
            .onSizeChanged {
                layoutHeight.value = it.height
            },
        content = {
            if (shouldRenderBody) {
                Box {
                    savedState.apply(Body, bodyContent)()
                }
            } else {
                Spacer(modifier = Modifier)
            }

            if (shouldRenderShim) {
                Canvas(
                    modifier = Modifier.fillMaxSize()
                        .tapGestureFilter {
                            // Do nothing -- just make sure tap events can't go through the shim
                        }
                ) {
                    drawRect(scrimColor, alpha = state.expansionPercentage)
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
                        onDrag = { dY ->
                            val distanceToExpandOver =
                                layoutHeight.value - collapsedSheetHeight.value
                            val expansionChange = -dY / distanceToExpandOver
                            val newExpansion = state.expansionPercentage + expansionChange
                            state.snapTo(CollapsingPageValue(newExpansion.coerceIn(0.0f, 1.0f)))
                        },
                        onDragStopped = { velocity ->
                            // TODO: There's a small bug here where the velocity is sometimes zero.
                            //  This causes the bottom sheet to immediately return where it was,
                            //  regardless of the actual swipe speed. This might be fixed in a
                            //  future Compose release. Alternatively, there might be a performance
                            //  issue that's causing too many touch samples to be dropped.
                            val completionAction = onStateChanged?.let { { it(state.value) } }
                            when {
                                velocity < 0 -> state.expand(completionAction)
                                velocity > 0 -> state.collapse(completionAction)
                                state.expansionPercentage > 0.5 -> state.expand(completionAction)
                                else -> state.collapse(completionAction)
                            }
                        }
                    )
            ) {
                PartiallyCollapsedPageLayout(
                    collapsedSheetLayout = savedState.apply(CollapsedContent, collapsedSheetLayout),
                    expandedSheetLayout = savedState.apply(BottomSheetContent, expandedSheetLayout),
                    percentExpanded = state.expansionPercentage,
                    onCollapsedSheetLayoutMeasured = { _, heightPx ->
                        collapsedSheetHeight.value = heightPx
                    }
                )
            }
        },
        measureBlocks = remember(state) {
            MeasuringIntrinsicsMeasureBlocks { measurables, constraints ->
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

@OptIn(ExperimentalLayoutNodeApi::class)
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
        measureBlocks = remember(percentExpanded) {
            MeasuringIntrinsicsMeasureBlocks { measurables, constraints ->
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

inline class CollapsingPageValue(
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
    val clock = AmbientAnimationClock.current.asDisposableClock()
    return rememberSavedInstanceState(
        inputs = arrayOf(clock),
        saver = CollapsingPageState.Saver(clock)
    ) {
        CollapsingPageState(initialValue, clock)
    }
}

class CollapsingPageState(
    initialValue: CollapsingPageValue,
    clock: AnimationClockObservable
) {

    var value by mutableStateOf(initialValue)

    private val animator: AnimatedFloat = object : AnimatedFloat(
        clock = clock
    ) {
        override var value: Float = initialValue.visibilityPercentage
            set(value) {
                field = value
                this@CollapsingPageState.value = CollapsingPageValue(value)
            }
    }

    val expansionPercentage: Float
        get() = value.visibilityPercentage

    val isFullyExpanded: Boolean
        get() = value == CollapsingPageValue.expanded

    val isFullyCollapsed: Boolean
        get() = value == CollapsingPageValue.collapsed

    val isPartiallyExpanded: Boolean
        get() = !isFullyExpanded && !isFullyCollapsed

    fun snapTo(targetValue: CollapsingPageValue) {
        value = targetValue
        animator.snapTo(targetValue.visibilityPercentage)
    }

    fun animateTo(
        targetValue: CollapsingPageValue,
        anim: AnimationSpec<Float> = SpringSpec(
            stiffness = 500f
        ),
        onEnd: ((AnimationEndReason, CollapsingPageValue) -> Unit)? = null
    ) {
        animator.animateTo(
            targetValue = targetValue.visibilityPercentage,
            anim = anim,
            onEnd = { endReason, endValue ->
                value = CollapsingPageValue(endValue)
                onEnd?.invoke(endReason, value)
            }
        )
    }

    fun expand(onOpened: (() -> Unit)? = null) {
        animateTo(
            CollapsingPageValue.expanded,
            onEnd = { endReason, endValue ->
                if (endReason != Interrupted && endValue == CollapsingPageValue.expanded) {
                    onOpened?.invoke()
                }
            }
        )
    }

    fun collapse(onClosed: (() -> Unit)? = null) {
        animateTo(
            CollapsingPageValue.collapsed,
            onEnd = { endReason, endValue ->
                if (endReason != Interrupted && endValue == CollapsingPageValue.expanded) {
                    onClosed?.invoke()
                }
            }
        )
    }

    companion object {
        fun Saver(
            clock: AnimationClockObservable
        ) = Saver<CollapsingPageState, Float>(
            save = { it.value.visibilityPercentage },
            restore = { CollapsingPageState(CollapsingPageValue(it), clock) }
        )
    }
}

private enum class BottomSheetSaveStateKey {
    Body,
    CollapsedContent,
    BottomSheetContent
}

@ExperimentalRestorableStateHolder
private inline fun RestorableStateHolder<BottomSheetSaveStateKey>.apply(
    key: BottomSheetSaveStateKey,
    crossinline action: @Composable () -> Unit
): @Composable () -> Unit {
    return { RestorableStateProvider(key) { action() } }
}
