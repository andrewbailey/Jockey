package dev.andrewbailey.music.ui.layout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.google.accompanist.insets.statusBarsHeight

@Composable
fun StatusBarBackground(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colors.run { if (isLight) primaryVariant else surface }
) {
    Box(
        modifier = modifier
            .background(color)
            .statusBarsHeight()
            .fillMaxWidth()
    )
}
