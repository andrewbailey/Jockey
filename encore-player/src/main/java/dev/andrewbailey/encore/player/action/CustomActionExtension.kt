package dev.andrewbailey.encore.player.action

import dev.andrewbailey.encore.player.playback.PlaybackExtension
import dev.andrewbailey.encore.player.state.MediaPlayerState

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

    override fun onNewPlayerState(newState: MediaPlayerState) {
        updateActions(newState)
    }

    internal suspend fun executeCustomAction(actionId: String) {
        val actionProvider = providers.firstOrNull { it.id == actionId }
            ?: throw IllegalArgumentException("No action provider with id $actionId is registered")

        actionProvider.performAction(getCurrentPlaybackState())
        updateActions(getCurrentPlaybackState())
    }

    private fun updateActions(newState: MediaPlayerState) {
        actions = providers.mapNotNull { it.getAction(newState) }
    }

}
