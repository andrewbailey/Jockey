package dev.andrewbailey.music.ui.library

import androidx.compose.foundation.Text
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.andrewbailey.music.R
import dev.andrewbailey.music.ui.library.songs.AllSongsRoot
import dev.andrewbailey.music.ui.navigation.AppNavigator
import dev.andrewbailey.music.ui.navigation.LibraryPage
import dev.andrewbailey.music.ui.navigation.NowPlayingScreen
import dev.andrewbailey.music.ui.navigation.RootScreen

@Composable
fun LibraryRoot(
    page: LibraryPage
) {
    val navigator = AppNavigator.current

    Column {
        TopAppBar(
            title = { Text(stringResource(R.string.app_name)) }
        )

        Surface(Modifier.weight(1f)) {
            val contentModifier = Modifier.fillMaxSize()

            when (page) {
                LibraryPage.Songs -> AllSongsRoot(
                    modifier = contentModifier
                )
                else -> {
                    Box(
                        modifier = contentModifier,
                        alignment = Alignment.Center
                    ) {
                        Text("Not yet implemented.")
                    }
                }
            }
        }

        Surface(elevation = 32.dp) {
            Column {
                Surface {
                    CollapsedPlayerControls(
                        modifier = Modifier.clickable(onClick = {
                            navigator.push(NowPlayingScreen)
                        })
                    )
                }

                Surface {
                    LibraryNavigationBar(
                        selectedPage = page,
                        onSelectLibraryPage = { newPage ->
                            navigator.replace(RootScreen(newPage))
                        }
                    )
                }
            }
        }
    }
}
