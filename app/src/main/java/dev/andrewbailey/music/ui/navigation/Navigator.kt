package dev.andrewbailey.music.ui.navigation

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.SaveableStateHolder
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import java.util.Stack
import java.util.UUID
import kotlinx.parcelize.Parcelize

val LocalAppNavigator = compositionLocalOf<Navigator> { error("No navigator has been set") }

class Navigator private constructor(
    private val stateHolder: SaveableStateHolder,
    private val backStack: Stack<BackStackEntry>
) {

    init {
        require(backStack.isNotEmpty()) {
            "The backstack must be initialized with at least one item"
        }
    }

    private var popOverrides = Stack<() -> Boolean>()

    private var currentScreen by mutableStateOf<BackStackEntry>(backStack.peek())

    fun push(screen: Screen) {
        backStack.push(BackStackEntry(screen))
        currentScreen = backStack.peek()
    }

    fun pop(): Boolean {
        popOverrides.asReversed().forEach { override ->
            if (override()) {
                return true
            }
        }

        if (backStack.size > 1) {
            backStack.pop()
            currentScreen = backStack.peek()
            return true
        }

        return false
    }

    @Composable
    fun render(
        content: @Composable (Screen) -> Unit
    ) {
        with(stateHolder) {
            SaveableStateProvider(key = currentScreen.uuid) {
                content(currentScreen.screen)
            }
        }
    }

    @Composable
    fun overridePopBehavior(
        navigateUp: () -> Boolean
    ) {
        DisposableEffect(navigateUp) {
            popOverrides.push(navigateUp)
            onDispose {
                popOverrides.remove(navigateUp)
            }
        }
    }

    companion object {
        @Composable
        fun rememberNavigator(
            initialBackStack: Collection<Screen> = listOf(RootScreen)
        ): Navigator {
            val saveableStateHolder = rememberSaveableStateHolder()
            return rememberSaveable(
                init = {
                    Navigator(
                        stateHolder = saveableStateHolder,
                        backStack = initialBackStack.map { BackStackEntry(it) }.toStack()
                    )
                },
                saver = Saver(saveableStateHolder)
            )
        }

        fun Saver(
            saveableStateHolder: SaveableStateHolder
        ) = Saver<Navigator, List<BackStackEntry>>(
            save = { it.backStack.toList() },
            restore = { Navigator(saveableStateHolder, it.toStack()) }
        ) as Saver<Navigator, *>
    }
}

@Parcelize
private class BackStackEntry(
    val screen: Screen,
    val uuid: UUID = UUID.randomUUID()
) : Parcelable

private fun <T> Collection<T>.toStack() = Stack<T>().also { it.addAll(this) }
