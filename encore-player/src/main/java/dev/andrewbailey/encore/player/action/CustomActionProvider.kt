package dev.andrewbailey.encore.player.action

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import dev.andrewbailey.encore.model.MediaObject
import dev.andrewbailey.encore.player.state.MediaPlayerState

public abstract class CustomActionProvider<M : MediaObject>(
    internal val id: String,
    /**
     * Determines whether other applications can execute this custom action. If set to true, this
     * action will be exposed on the associated MediaSession. Actions that are not exposed can be
     * used in notifications.
     */
    private val exposed: Boolean = true
) {

    internal fun getAction(state: MediaPlayerState<M>): CustomAction? {
        return if (isEnabled(state)) {
            CustomAction(
                id = id,
                name = getActionName(state),
                icon = getActionIcon(state),
                exposed = exposed
            )
        } else {
            null
        }
    }

    internal suspend fun performAction(state: MediaPlayerState<M>) {
        onPerformCustomAction(state)
    }

    @StringRes
    protected abstract fun getActionName(state: MediaPlayerState<M>): Int

    @DrawableRes
    protected abstract fun getActionIcon(state: MediaPlayerState<M>): Int

    protected open fun isEnabled(state: MediaPlayerState<M>): Boolean {
        return true
    }

    protected abstract suspend fun onPerformCustomAction(state: MediaPlayerState<M>)

}
