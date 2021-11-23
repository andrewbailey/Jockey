package dev.andrewbailey.music.ui.library

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.andrewbailey.music.ui.navigation.LibraryPage
import dev.andrewbailey.music.ui.navigation.LibraryPage.Albums
import dev.andrewbailey.music.ui.navigation.LibraryPage.Artists
import dev.andrewbailey.music.ui.navigation.LibraryPage.Folders
import dev.andrewbailey.music.ui.navigation.LibraryPage.Playlists
import dev.andrewbailey.music.ui.navigation.LibraryPage.Songs

@Composable
fun LibraryNavigationBar(
    selectedPage: LibraryPage,
    modifier: Modifier = Modifier,
    onSelectLibraryPage: (LibraryPage) -> Unit = {}
) {
    Row(
        modifier = modifier
            .height(56.dp)
            .padding(start = 12.dp, end = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        listOf(Playlists, Songs, Albums, Artists, Folders).forEach { page ->
            LibraryNavigationOption(
                page = page,
                isSelected = page == selectedPage,
                modifier = Modifier
                    .weight(1f, true),
                onSelected = onSelectLibraryPage
            )
        }
    }
}

@Composable
private fun LibraryNavigationOption(
    page: LibraryPage,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onSelected: (LibraryPage) -> Unit = {}
) {
    Column(
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(
                    bounded = false,
                    color = MaterialTheme.colors.primary
                ),
                onClick = { onSelected(page) }
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val selectionColor = MaterialTheme.colors.primary
        val unselectedColor = MaterialTheme.colors.onSurface.copy(
            alpha = if (MaterialTheme.colors.isLight) {
                0.5f
            } else {
                0.6f
            }
        )

        val selectionStateTransition = updateTransition(
            targetState = isSelected,
            label = "IsSelectedTransition"
        )

        val iconColor by selectionStateTransition.animateColor(
            label = "IconColorTransition",
            transitionSpec = {
                tween(
                    durationMillis = 200,
                    easing = if (targetState.isTransitioningTo(true)) {
                        LinearOutSlowInEasing
                    } else {
                        FastOutLinearInEasing
                    }
                )
            }
        ) { selected ->
            if (selected) selectionColor else unselectedColor
        }

        val textColor by selectionStateTransition.animateColor(
            label = "TextColorTransition",
            transitionSpec = {
                tween(
                    durationMillis = 200,
                    easing = if (targetState.isTransitioningTo(true)) {
                        LinearOutSlowInEasing
                    } else {
                        FastOutLinearInEasing
                    }
                )
            }
        ) { selected ->
            if (selected) selectionColor else unselectedColor.copy(alpha = 0f)
        }

        val iconTranslation by selectionStateTransition.animateDp(
            label = "IconColorTransition",
            transitionSpec = {
                tween(
                    durationMillis = 200,
                    easing = if (targetState.isTransitioningTo(true)) {
                        LinearOutSlowInEasing
                    } else {
                        FastOutLinearInEasing
                    }
                )
            }
        ) { selected ->
            if (selected) 0.dp else 8.dp
        }

        LibraryNavigationIcon(
            page = page,
            tint = iconColor,
            modifier = Modifier
                .offset(y = iconTranslation)
        )

        LibraryNavigationLabel(
            page = page,
            textColor = textColor,
            modifier = Modifier.offset(y = iconTranslation)
        )
    }
}

@Composable
private fun LibraryNavigationIcon(
    page: LibraryPage,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Icon(
        painter = painterResource(page.iconRes),
        contentDescription = stringResource(page.labelRes),
        tint = tint,
        modifier = modifier
    )
}

@Composable
private fun LibraryNavigationLabel(
    page: LibraryPage,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Text(
        style = MaterialTheme.typography.caption,
        maxLines = 1,
        color = textColor,
        modifier = modifier,
        text = stringResource(page.labelRes)
    )
}
