package dev.andrewbailey.music.ui

import android.os.Build
import androidx.annotation.ColorRes
import androidx.annotation.RequiresApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import dev.andrewbailey.music.R
import dev.andrewbailey.music.util.fromRes

@Composable
fun JockeyTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colors = jockeyColorScheme()
    ) {
        content()
    }
}

@Composable
private fun jockeyColorScheme(): Colors {
    return if (isSystemInDarkTheme()) {
        darkJockeyColorScheme()
    } else {
        lightJockeyColorScheme()
    }
}

@Composable
private fun darkJockeyColorScheme(): Colors {
    return if (Build.VERSION.SDK_INT >= 31) {
        dynamicDarkColorScheme()
    } else {
        darkColors(
            primary = Color(0xFF5EBDEE),
            secondary = Color(0xFF76D9E6),
            onSecondary = Color(0xFF00585D)
        )
    }
}

@Composable
private fun lightJockeyColorScheme(): Colors {
    return if (Build.VERSION.SDK_INT >= 31) {
        dynamicLightColorScheme()
    } else {
        lightColors(
            primary = Color(0xFF3282C3),
            secondary = Color(0xFF1BD2EB),
            onSecondary = Color(0xFF00636C)
        )
    }
}

@RequiresApi(Build.VERSION_CODES.S)
@Composable
private fun dynamicLightColorScheme() = lightColors(
    primary = colorResource(android.R.color.system_accent1_600),
    onPrimary = colorResource(android.R.color.system_accent1_0),
    primaryVariant = colorResource(android.R.color.system_accent1_700),
    secondary = colorResource(android.R.color.system_accent3_600),
    onSecondary = colorResource(android.R.color.system_accent3_0),
    secondaryVariant = colorResource(android.R.color.system_accent3_700),
    background = colorResource(android.R.color.system_neutral1_10),
    onBackground = colorResource(android.R.color.system_neutral1_900),
    surface = colorResource(android.R.color.system_neutral1_10),
    onSurface = colorResource(android.R.color.system_neutral1_900)
)

@RequiresApi(Build.VERSION_CODES.S)
@Composable
private fun dynamicDarkColorScheme() = darkColors(
    primary = colorResource(android.R.color.system_accent1_200),
    onPrimary = colorResource(android.R.color.system_accent1_800),
    primaryVariant = colorResource(android.R.color.system_accent1_300),
    secondary = colorResource(android.R.color.system_accent3_200),
    onSecondary = colorResource(android.R.color.system_accent3_800),
    secondaryVariant = colorResource(android.R.color.system_accent3_300),
    background = colorResource(android.R.color.system_neutral1_900),
    onBackground = colorResource(android.R.color.system_neutral1_10),
    surface = colorResource(android.R.color.system_neutral1_900),
    onSurface = colorResource(android.R.color.system_neutral1_10)
)

@Composable
private fun colorResource(@ColorRes colorRes: Int) = Color.fromRes(LocalContext.current, colorRes)
