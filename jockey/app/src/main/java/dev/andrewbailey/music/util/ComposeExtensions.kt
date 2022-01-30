package dev.andrewbailey.music.util

import android.annotation.SuppressLint
import android.content.Context
import androidx.annotation.ColorRes
import androidx.annotation.FloatRange
import androidx.annotation.IntRange
import androidx.annotation.PluralsRes
import androidx.annotation.Px
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.neverEqualPolicy
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.SubcomposeMeasureScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.core.content.ContextCompat
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.WindowInsets
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect

/**
 * [androidx.compose.runtime.collectAsState] uses a structural equality snapshot policy, meaning
 * that if a Flow emits the same value twice, the resulting state will ignore the second emission.
 * This extension behaves the same, but indicates the [neverEqualPolicy], meaning that duplicate
 * emissions will propagate into the resulting UI state.
 *
 * Make sure you do not combine this with [kotlinx.coroutines.flow.StateFlow], since StateFlow will
 * _also_ ignore duplicate emissions.
 */
@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun <T : R, R> Flow<T>.collectAsNonUniqueState(initialValue: R): State<R> {
    val result = remember { mutableStateOf(initialValue, neverEqualPolicy()) }
    LaunchedEffect(this) {
        collect { result.value = it }
    }
    return result
}

@Composable
@ReadOnlyComposable
fun pluralsResource(@PluralsRes id: Int, quantity: Int): String {
    val resources = LocalContext.current.resources
    return resources.getQuantityString(id, quantity)
}

@Composable
@ReadOnlyComposable
fun pluralsResource(@PluralsRes id: Int, quantity: Int, vararg formatArgs: Any): String {
    val resources = LocalContext.current.resources
    return resources.getQuantityString(id, quantity, *formatArgs)
}

@Px
@Composable
fun heightOf(content: @Composable () -> Unit): Int {
    var height by remember { mutableStateOf(0) }
    Layout(
        content = {
            content()
        },
        measurePolicy = { measurables, constraints ->
            height = measurables.first().measure(constraints).height
            layout(0, 0) {}
        }
    )
    return height
}

fun SubcomposeMeasureScope.subcomposeSingle(
    slotId: Any?,
    content: @Composable () -> Unit
): Measurable {
    return subcompose(slotId, content).also {
        require(it.size == 1) {
            "The content block should have exactly one child."
        }
    }.first()
}

fun Color.Companion.fromRes(
    context: Context,
    @ColorRes colorRes: Int
) = Color(ContextCompat.getColor(context, colorRes))

fun Color.copy(
    @IntRange(from = 0, to = 360)
    hue: Int = this.hue,
    @FloatRange(from = 0.0, to = 1.0)
    saturation: Float = this.saturation,
    @FloatRange(from = 0.0, to = 1.0)
    luminance: Float = this.luminance
): Color {
    val chroma = (1 - abs(2 * luminance - 1)) * saturation
    val lightness = luminance - chroma / 2
    val secondaryColor = chroma * (1 - abs((hue / 60f) % 2 - 1))

    val (r, g, b) = when (hue) {
        in 0 until 60 -> floatArrayOf(chroma, secondaryColor, 0f)
        in 60 until 120 -> floatArrayOf(secondaryColor, chroma, 0f)
        in 120 until 180 -> floatArrayOf(0f, chroma, secondaryColor)
        in 180 until 240 -> floatArrayOf(0f, secondaryColor, chroma)
        in 240 until 300 -> floatArrayOf(secondaryColor, 0f, chroma)
        in 300 until 360 -> floatArrayOf(chroma, 0f, secondaryColor)
        else -> throw IllegalArgumentException("Invalid hue $hue (must be between 0-360)")
    }

    return Color(
        red = ((r + lightness) * 255).roundToInt(),
        green = ((g + lightness) * 255).roundToInt(),
        blue = ((b + lightness) * 255).roundToInt()
    )
}

val Color.hue: Int
    get() {
        val chromaMax = maxOf(red, green, blue)
        val chromaMin = minOf(red, green, blue)
        val chromaDelta = chromaMax - chromaMin

        val hue = when (chromaMax) {
            chromaMin -> 0f
            red -> ((green - blue) / chromaDelta).floorMod(6)
            green -> (blue - red) / chromaDelta + 2
            blue -> (red - green) / chromaDelta + 4
            else -> 0f
        } * 60

        return hue.toInt()
    }

val Color.saturation: Float
    get() {
        val chromaMax = maxOf(red, green, blue)
        val chromaMin = minOf(red, green, blue)

        return when (val chromaDelta = chromaMax - chromaMin) {
            0f -> 0f
            else -> chromaDelta / (1 - abs(2 * luminance - 1))
        }
    }

val Color.luminance: Float
    get() = (maxOf(red, green, blue) + minOf(red, green, blue)) / 2

@Composable
operator fun PaddingValues.plus(other: PaddingValues) = PaddingValues(
    start = this.calculateStartPadding(LocalLayoutDirection.current) +
        other.calculateStartPadding(LocalLayoutDirection.current),
    top = this.calculateTopPadding() +
        other.calculateTopPadding(),
    end = this.calculateStartPadding(LocalLayoutDirection.current) +
        other.calculateStartPadding(LocalLayoutDirection.current),
    bottom = this.calculateBottomPadding() +
        other.calculateBottomPadding(),
)

@Composable
fun ConsumeWindowInsets(
    leftPx: Int = 0,
    topPx: Int = 0,
    rightPx: Int = 0,
    bottomPx: Int = 0,
    content: @Composable () -> Unit
) {
    val currentWindowInsets = LocalWindowInsets.current

    CompositionLocalProvider(
        LocalWindowInsets provides RelativeInsets(
            originalInsets = currentWindowInsets,
            left = -leftPx,
            top = -topPx,
            right = -rightPx,
            bottom = -bottomPx
        )
    ) {
        content()
    }
}

private class RelativeInsets(
    originalInsets: WindowInsets,
    left: Int,
    top: Int,
    right: Int,
    bottom: Int
) : WindowInsets {
    override val displayCutout: WindowInsets.Type =
        RelativeInsetsType(originalInsets.displayCutout, left, top, right, bottom)
    override val ime: WindowInsets.Type =
        RelativeInsetsType(originalInsets.ime, left, top, right, bottom)
    override val navigationBars: WindowInsets.Type =
        RelativeInsetsType(originalInsets.navigationBars, left, top, right, bottom)
    override val statusBars: WindowInsets.Type =
        RelativeInsetsType(originalInsets.statusBars, left, top, right, bottom)
    override val systemBars: WindowInsets.Type =
        RelativeInsetsType(originalInsets.systemBars, left, top, right, bottom)
    override val systemGestures: WindowInsets.Type =
        RelativeInsetsType(originalInsets.systemGestures, left, top, right, bottom)
}

private class RelativeInsetsType(
    private val originalInsets: WindowInsets.Type,
    private val dLeft: Int,
    private val dTop: Int,
    private val dRight: Int,
    private val dBottom: Int
) : WindowInsets.Type by originalInsets {
    override val left: Int get() = (originalInsets.left + dLeft).coerceAtLeast(0)
    override val top: Int get() = (originalInsets.top + dTop).coerceAtLeast(0)
    override val right: Int get() = (originalInsets.right + dRight).coerceAtLeast(0)
    override val bottom: Int get() = (originalInsets.bottom + dBottom).coerceAtLeast(0)
}
