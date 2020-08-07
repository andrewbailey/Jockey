package dev.andrewbailey.music.ui.core

import android.content.Context
import androidx.compose.material.lightColors
import androidx.compose.ui.graphics.Color
import androidx.fragment.app.Fragment
import dev.andrewbailey.music.R
import dev.andrewbailey.music.util.fromRes

fun Fragment.colorPalette() = colorPalette(requireContext())

fun colorPalette(context: Context) = lightColors(
    primary = Color.fromRes(context, R.color.colorPrimary),
    primaryVariant = Color.fromRes(context, R.color.colorPrimary),
    secondary = Color.fromRes(context, R.color.colorAccent),
    secondaryVariant = Color.fromRes(context, R.color.colorAccentDark)
)
