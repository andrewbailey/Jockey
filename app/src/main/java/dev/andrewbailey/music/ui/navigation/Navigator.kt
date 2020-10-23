package dev.andrewbailey.music.ui.navigation

import androidx.compose.runtime.*
import java.util.Stack

val AppNavigator = ambientOf<Navigator> { error("No navigator has been set") }

class Navigator private constructor(
    initialScreens: Collection<Screen>
) {

    private val backStack = Stack<Screen>().apply {
        addAll(initialScreens)
    }

    var currentScreen by mutableStateOf<Screen>(backStack.peek())
        private set

    val canNavigateUp: Boolean
        get() = backStack.size > 1

    constructor() : this(listOf(RootScreen(LibraryPage.Songs)))

    fun navigateUp() {
        if (canNavigateUp) {
            backStack.pop()
            invalidateCurrentScreen()
        }
    }

    fun push(screen: Screen) {
        backStack.push(screen)
        invalidateCurrentScreen()
    }

    fun replace(screen: Screen) {
        backStack.pop()
        backStack.push(screen)
        invalidateCurrentScreen()
    }

    private fun invalidateCurrentScreen() {
        currentScreen = backStack.peek()
    }

}
