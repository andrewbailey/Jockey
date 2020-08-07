package dev.andrewbailey.music.util

import android.content.Context
import androidx.annotation.ColorRes
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer

@Composable
fun <T> observe(data: LiveData<T>): T? {
    var result by state(neverEqualPolicy()) { data.value }
    val observer = remember { Observer<T> { result = it } }

    onCommit(data) {
        data.observeForever(observer)
        onDispose { data.removeObserver(observer) }
    }

    return result
}

fun Color.Companion.fromRes(
    context: Context,
    @ColorRes colorRes: Int
) = Color(ContextCompat.getColor(context, colorRes))
