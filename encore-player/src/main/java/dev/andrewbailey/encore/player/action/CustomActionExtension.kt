package dev.andrewbailey.encore.player.action

import dev.andrewbailey.encore.player.playback.PlaybackExtension
import dev.andrewbailey.encore.player.state.PlaybackState

internal class CustomActionExtension(
    private val providers: List<CustomActionProvider>,
    private val onActionsChanged: (List<CustomAction>) -> Unit
) : PlaybackExtension() {

    private var actions: List<CustomAction> = emptyList()
        set(value) {
            if (field != value) {
                field = value
                onActionsChanged(value)
            }
        }

    override fun onNewPlayerState(newState: PlaybackState): PlaybackState {
        updateActions(newState)
        return super.onNewPlayerState(newState)
    }

    internal suspend fun executeCustomAction(actionId: String) {
        val actionProvider = providers.firstOrNull { it.id == actionId }
            ?: throw IllegalArgumentException("No action provider with id $actionId is registered")

        actionProvider.performAction(getCurrentPlaybackState())
        updateActions(getCurrentPlaybackState())
    }

    private fun updateActions(newState: PlaybackState) {
        actions = providers.mapNotNull { it.getAction(newState) }
    }

}
