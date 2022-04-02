package dev.andrewbailey.encore.player.browse

import dev.andrewbailey.encore.model.MediaObject
import dev.andrewbailey.encore.player.playback.PlaybackExtension
import dev.andrewbailey.encore.player.state.MediaPlayerState
import dev.andrewbailey.encore.player.state.TransportState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

public abstract class MediaResumptionProvider<M : MediaObject> {

    private val extension = Extension()
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private var lastPersistedState: TransportState<M>? = null
    private var wasInitialStateSet = false

    private var saveStateJob: Job? = null
        set(value) {
            field?.cancel()
            field = value
        }

    public abstract suspend fun persistState(transportState: TransportState<M>): Boolean

    public abstract suspend fun getCurrentTrack(): M?

    public abstract suspend fun getPersistedTransportState(): TransportState<M>?

    internal fun asPlaybackExtension(): PlaybackExtension<M> = extension

    private inner class Extension : PlaybackExtension<M>() {
        override fun onPrepared() {
            coroutineScope.launch {
                getPersistedTransportState()?.let {
                    lastPersistedState = it
                    withContext(Dispatchers.Main) {
                        setTransportState(it)
                    }
                }
                wasInitialStateSet = true
            }
        }

        override fun onNewPlayerState(newState: MediaPlayerState<M>) {
            if (wasInitialStateSet && newState != lastPersistedState) {
                saveStateJob = coroutineScope.launch {
                    if (persistState(newState.transportState)) {
                        lastPersistedState = newState.transportState
                    }
                }
            }
        }

        override fun onRelease() {
            coroutineScope.cancel()
        }
    }
}
