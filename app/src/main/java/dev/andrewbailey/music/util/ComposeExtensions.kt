package dev.andrewbailey.music.util

import android.content.Context
import androidx.annotation.ColorRes
import androidx.annotation.FloatRange
import androidx.annotation.IntRange
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.neverEqualPolicy
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun <T> observe(data: LiveData<T>): T? {
    var result by remember { mutableStateOf(data.value, neverEqualPolicy()) }
    val observer = remember { Observer<T> { result = it } }

    DisposableEffect(data) {
        data.observeForever(observer)
        onDispose { data.removeObserver(observer) }
    }

    return result
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
        val chromaDelta = chromaMax - chromaMin

        return when (chromaDelta) {
            0f -> 0f
            else -> chromaDelta / (1 - abs(2 * luminance - 1))
        }
    }

val Color.luminance: Float
    get() = (maxOf(red, green, blue) + minOf(red, green, blue)) / 2
