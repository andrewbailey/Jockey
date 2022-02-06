package dev.andrewbailey.music.ui.library

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.AppBarDefaults
import androidx.compose.material.LocalElevationOverlay
import androidx.compose.material.MaterialTheme
import androidx.compose.material.TopAppBar
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import dev.andrewbailey.music.ui.layout.StatusBarBackground

@Composable
fun LibraryAppBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    elevation: Dp = AppBarDefaults.TopAppBarElevation
) {
    val surfaceColor = LocalElevationOverlay.current?.apply(
        color = MaterialTheme.colors.surface,
        elevation = elevation
    ) ?: MaterialTheme.colors.surface

    Column(
        modifier = modifier
            .shadow(elevation)
            .zIndex(1f)
    ) {
        StatusBarBackground(
            color = surfaceColor
        )

        TopAppBar(
            title = title,
            navigationIcon = navigationIcon,
            actions = actions,
            backgroundColor = surfaceColor,
            contentColor = contentColorFor(MaterialTheme.colors.surface),
            elevation = 0.dp,
        )
    }
}
