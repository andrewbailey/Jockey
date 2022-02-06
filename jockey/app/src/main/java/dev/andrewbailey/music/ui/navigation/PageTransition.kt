package dev.andrewbailey.music.ui.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.with
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun <S> PageTransition(
    targetState: S,
    modifier: Modifier = Modifier,
    reverseAnimation: (initialState: S, targetState: S) -> Boolean = { _, _ -> false },
    slidePercentage: Float = 0.08f,
    content: @Composable AnimatedVisibilityScope.(targetState: S) -> Unit
) {
    val reverseAnimationCallback by rememberUpdatedState(reverseAnimation)

    AnimatedContent(
        targetState = targetState,
        transitionSpec = {
            val reverse = reverseAnimationCallback(initialState, targetState)

            val enterAnimation = if (reverse) {
                fadeIn()
            } else {
                fadeIn() + slideInVertically(initialOffsetY = { (it * slidePercentage).toInt() })
            }

            val exitAnimation = if (reverse) {
                fadeOut() + slideOutVertically(targetOffsetY = { (it * slidePercentage).toInt() })
            } else {
                fadeOut()
            }

            enterAnimation with exitAnimation
        },
        modifier = modifier,
        content = content
    )
}
