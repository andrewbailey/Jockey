package dev.andrewbailey.encore.mediaresumption

import android.content.Context
import dev.andrewbailey.encore.mediaresumption.impl.PersistedMediaStateRepository
import dev.andrewbailey.encore.model.MediaObject
import dev.andrewbailey.encore.player.browse.MediaResumptionProvider
import dev.andrewbailey.encore.player.state.MediaPlaybackState
import dev.andrewbailey.encore.provider.MediaProvider

public class PlaybackStateSaver<M : MediaObject>(
    context: Context,
    mediaProvider: MediaProvider<M>
) : MediaResumptionProvider<M>() {

    private val stateRepository = PersistedMediaStateRepository(context, mediaProvider)

    override suspend fun persistState(mediaPlaybackState: MediaPlaybackState<M>): Boolean {
        stateRepository.saveState(mediaPlaybackState)
        return true
    }

    override suspend fun getCurrentTrack(): M? {
        return stateRepository.getLastPlayingItem()
    }

    override suspend fun getPersistedMediaPlaybackState(): MediaPlaybackState<M>? {
        return stateRepository.getState()
    }
}
