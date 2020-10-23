package dev.andrewbailey.music.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Providers
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.setContent
import dagger.hilt.android.AndroidEntryPoint
import dev.andrewbailey.music.R
import dev.andrewbailey.music.ui.navigation.AppNavigator
import dev.andrewbailey.music.ui.navigation.Navigator
import dev.andrewbailey.music.ui.root.JockeyRoot
import dev.andrewbailey.music.util.fromRes

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var navigator: Navigator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        navigator = Navigator()
        setContent {
            MaterialTheme(
                colors = colors()
            ) {
                Providers(
                    AppNavigator provides navigator
                ) {
                    JockeyRoot()
                }
            }
        }
    }

    override fun onBackPressed() {
        if (navigator.canNavigateUp) {
            navigator.navigateUp()
        } else {
            super.onBackPressed()
        }
    }

    @Composable
    private fun colors() = if (isSystemInDarkTheme()) {
        darkColors(
            primary = Color.fromRes(this, R.color.colorPrimary),
            primaryVariant = Color.fromRes(this, R.color.colorPrimary),
            secondary = Color.fromRes(this, R.color.colorAccent)
        )
    } else {
        lightColors(
            primary = Color.fromRes(this, R.color.colorPrimary),
            primaryVariant = Color.fromRes(this, R.color.colorPrimary),
            secondary = Color.fromRes(this, R.color.colorAccent),
            secondaryVariant = Color.fromRes(this, R.color.colorAccentDark)
        )
    }

}
