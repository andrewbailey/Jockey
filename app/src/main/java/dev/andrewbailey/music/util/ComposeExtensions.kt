package dev.andrewbailey.music.util

import android.content.Context
import androidx.annotation.ColorRes
import androidx.compose.effectOf
import androidx.compose.memo
import androidx.compose.onCommit
import androidx.compose.state
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.ui.graphics.Color

fun <T> observe(data: LiveData<T>) = effectOf<T?> {
    val result = +state { data.value }
    val observer = +memo { Observer<T> { result.value = it } }

    +onCommit(data) {
        data.observeForever(observer)
        onDispose { data.removeObserver(observer) }
    }

    result.value
}

fun Color.Companion.fromRes(
    context: Context,
    @ColorRes colorRes: Int
) = Color(ContextCompat.getColor(context, colorRes))
