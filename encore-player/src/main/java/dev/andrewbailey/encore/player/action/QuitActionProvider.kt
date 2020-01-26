package dev.andrewbailey.encore.player.action

import dev.andrewbailey.encore.player.MediaPlayerService
import dev.andrewbailey.encore.player.R
import dev.andrewbailey.encore.player.state.MediaPlayerState

internal class QuitActionProvider(
    private val service: MediaPlayerService
) : CustomActionProvider(
    id = ACTION_ID
) {
    override fun getActionName(state: MediaPlayerState) = R.string.encore_action_quit_service

    override fun getActionIcon(state: MediaPlayerState) = 0

    override suspend fun onPerformCustomAction(state: MediaPlayerState) {
        service.quit()
    }

    companion object {
        const val ACTION_ID = "quitService"
    }

}
