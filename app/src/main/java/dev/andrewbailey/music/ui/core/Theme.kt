package dev.andrewbailey.music.ui.core

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.ui.graphics.Color
import androidx.ui.material.lightColorPalette
import dev.andrewbailey.music.R
import dev.andrewbailey.music.util.fromRes

fun Fragment.colorPalette() = colorPalette(requireContext())

fun colorPalette(context: Context) = lightColorPalette(
    primary = Color.fromRes(context, R.color.colorPrimary),
    primaryVariant = Color.fromRes(context, R.color.colorPrimary),
    secondary = Color.fromRes(context, R.color.colorAccent),
    secondaryVariant = Color.fromRes(context, R.color.colorAccentDark)
)
