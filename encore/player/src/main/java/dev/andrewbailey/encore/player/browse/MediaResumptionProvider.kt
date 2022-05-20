package dev.andrewbailey.encore.player.browse

import dev.andrewbailey.encore.model.MediaObject
import dev.andrewbailey.encore.player.playback.PlaybackExtension
import dev.andrewbailey.encore.player.state.MediaPlaybackState
import dev.andrewbailey.encore.player.state.MediaPlayerState
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

public abstract class MediaResumptionProvider<M : MediaObject>(
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    private val extension = Extension()
    private val coroutineScope = CoroutineScope(dispatcher)

    private var lastPersistedState: MediaPlaybackState<M>? = null

    private var saveStateJob: Job? = null
        set(value) {
            field?.cancel()
            field = value
        }

    public abstract suspend fun persistState(mediaPlaybackState: MediaPlaybackState<M>): Boolean

    public abstract suspend fun getCurrentTrack(): M?

    public abstract suspend fun getPersistedTransportState(): MediaPlaybackState<M>?

    internal fun asPlaybackExtension(): PlaybackExtension<M> = extension

    private inner class Extension : PlaybackExtension<M>() {

        override suspend fun onInterceptInitializationState(
            pendingMediaPlaybackState: MediaPlaybackState<M>?
        ): MediaPlaybackState<M>? {
            return withContext(dispatcher) {
                getPersistedTransportState().also {
                    lastPersistedState = it
                }
            }
        }

        override fun onNewPlayerState(newState: MediaPlayerState.Initialized<M>) {
            if (newState != lastPersistedState) {
                saveStateJob = coroutineScope.launch {
                    if (persistState(newState.mediaPlaybackState)) {
                        lastPersistedState = newState.mediaPlaybackState
                    }
                }
            }
        }

        override fun onRelease() {
            coroutineScope.cancel()
        }
    }
}
