package dev.andrewbailey.music.util

import android.content.Context
import androidx.annotation.ColorRes
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
