package dev.andrewbailey.music.ui.navigation

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.ambientOf
import androidx.compose.runtime.savedinstancestate.ExperimentalRestorableStateHolder
import androidx.compose.runtime.savedinstancestate.RestorableStateHolder
import androidx.compose.runtime.savedinstancestate.Saver
import androidx.compose.runtime.savedinstancestate.rememberRestorableStateHolder
import androidx.compose.runtime.savedinstancestate.rememberSavedInstanceState
import java.util.Stack
import java.util.UUID
import kotlinx.android.parcel.Parcelize

val AppNavigator = ambientOf<Navigator> { error("No navigator has been set") }

@OptIn(ExperimentalRestorableStateHolder::class)
class Navigator private constructor(
    private val restorableStateHolder: RestorableStateHolder<UUID>,
    private val backStack: Stack<BackStackEntry>
) {

    init {
        require(backStack.isNotEmpty()) {
            "The backstack must be initialized with at least one item"
        }
    }

    private var popOverrides = Stack<() -> Boolean>()

    fun push(screen: Screen) {
        backStack.push(BackStackEntry(screen))
    }

    fun pop(): Boolean {
        popOverrides.asReversed().forEach { override ->
            if (override()) {
                return true
            }
        }

        if (backStack.size > 1) {
            backStack.pop()
            return true
        }

        return false
    }

    @Composable
    fun render(
        content: @Composable (Screen) -> Unit
    ) {
        with(restorableStateHolder) {
            val backStackEntry = backStack.peek()
            RestorableStateProvider(key = backStackEntry.uuid) {
                content(backStackEntry.screen)
            }
        }
    }

    @Composable
    fun overridePopBehavior(
        navigateUp: () -> Boolean
    ) {
        DisposableEffect(
            subject = navigateUp,
            effect = {
                popOverrides.push(navigateUp)
                onDispose {
                    popOverrides.remove(navigateUp)
                }
            }
        )
    }

    companion object {
        @Composable
        @OptIn(ExperimentalRestorableStateHolder::class)
        fun rememberNavigator(
            initialBackStack: Collection<Screen> = listOf(RootScreen)
        ): Navigator {
            val restorableStateHolder = rememberRestorableStateHolder<UUID>()
            return rememberSavedInstanceState(
                init = {
                    Navigator(
                        restorableStateHolder = restorableStateHolder,
                        backStack = initialBackStack.map { BackStackEntry(it) }.toStack()
                    )
                },
                saver = Saver(restorableStateHolder)
            )
        }

        fun Saver(
            restorableStateHolder: RestorableStateHolder<UUID>
        ) = Saver<Navigator, List<BackStackEntry>>(
            save = { it.backStack.toList() },
            restore = { Navigator(restorableStateHolder, it.toStack()) }
        ) as Saver<Navigator, *>
    }
}

@Parcelize
private class BackStackEntry(
    val screen: Screen,
    val uuid: UUID = UUID.randomUUID()
) : Parcelable

private fun <T> Collection<T>.toStack() = Stack<T>().also { it.addAll(this) }
