package dev.andrewbailey.encore.player.action

import dev.andrewbailey.encore.model.MediaItem
import dev.andrewbailey.encore.player.MediaPlayerService
import dev.andrewbailey.encore.player.R
import dev.andrewbailey.encore.player.state.MediaPlayerState

internal class QuitActionProvider<M : MediaItem>(
    private val service: MediaPlayerService<*>
) : CustomActionProvider<M>(
    id = ACTION_ID
) {
    override fun getActionName(state: MediaPlayerState<M>) = R.string.encore_action_quit_service

    override fun getActionIcon(state: MediaPlayerState<M>) = 0

    override suspend fun onPerformCustomAction(state: MediaPlayerState<M>) {
        service.quit()
    }

    companion object {
        const val ACTION_ID = "quitService"
    }

}
