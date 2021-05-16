package dev.andrewbailey.music.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat
import com.google.accompanist.insets.ProvideWindowInsets
import dagger.hilt.android.AndroidEntryPoint
import dev.andrewbailey.music.R
import dev.andrewbailey.music.ui.navigation.LocalAppNavigator
import dev.andrewbailey.music.ui.navigation.Navigator
import dev.andrewbailey.music.ui.navigation.Navigator.Companion.rememberNavigator
import dev.andrewbailey.music.ui.root.JockeyRoot
import dev.andrewbailey.music.util.fromRes

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var navigator: Navigator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            navigator = rememberNavigator()

            MaterialTheme(
                colors = colors()
            ) {
                ProvideWindowInsets {
                    CompositionLocalProvider(
                        LocalAppNavigator provides navigator
                    ) {
                        JockeyRoot()
                    }
                }
            }
        }
    }

    override fun onBackPressed() {
        if (!navigator.pop()) {
            super.onBackPressed()
        }
    }

    @Composable
    private fun colors() = if (isSystemInDarkTheme()) {
        darkColors(
            primary = Color.fromRes(this, R.color.colorPrimary),
            primaryVariant = Color.fromRes(this, R.color.colorPrimaryDark),
            secondary = Color.fromRes(this, R.color.colorAccent)
        )
    } else {
        lightColors(
            primary = Color.fromRes(this, R.color.colorPrimary),
            primaryVariant = Color.fromRes(this, R.color.colorPrimaryDark),
            secondary = Color.fromRes(this, R.color.colorAccent),
            secondaryVariant = Color.fromRes(this, R.color.colorAccentDark)
        )
    }

}
