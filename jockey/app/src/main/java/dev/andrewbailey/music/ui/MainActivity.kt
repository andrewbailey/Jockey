package dev.andrewbailey.music.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import dagger.hilt.android.AndroidEntryPoint
import dev.andrewbailey.music.ui.data.LocalMediaLibrary
import dev.andrewbailey.music.ui.data.LocalPlaybackController
import dev.andrewbailey.music.ui.data.MediaLibrary
import dev.andrewbailey.music.ui.data.PlaybackController
import dev.andrewbailey.music.ui.navigation.LocalAppNavigator
import dev.andrewbailey.music.ui.navigation.Navigator
import dev.andrewbailey.music.ui.navigation.Navigator.Companion.rememberNavigator
import dev.andrewbailey.music.ui.root.JockeyRoot
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var mediaLibrary: MediaLibrary
    @Inject lateinit var playbackController: PlaybackController

    private lateinit var navigator: Navigator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            navigator = rememberNavigator()

            JockeyTheme {
                CompositionLocalProvider(
                    LocalAppNavigator provides navigator,
                    LocalMediaLibrary provides mediaLibrary,
                    LocalPlaybackController provides playbackController
                ) {
                    JockeyRoot(
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }

    override fun onBackPressed() {
        if (!navigator.pop()) {
            super.onBackPressed()
        }
    }

}
