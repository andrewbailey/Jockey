package dev.andrewbailey.encore.player.playback

import android.util.Log
import dev.andrewbailey.encore.model.MediaObject
import dev.andrewbailey.encore.player.state.MediaPlayerState
import dev.andrewbailey.encore.player.state.RepeatMode
import dev.andrewbailey.encore.player.state.ShuffleMode
import dev.andrewbailey.encore.player.state.TransportState
import dev.andrewbailey.encore.player.state.factory.PlaybackStateFactory

public abstract class PlaybackExtension<M : MediaObject> {

    private var mediaPlayer: MediaPlayer<M>? = null
    private var playbackStateFactory: PlaybackStateFactory<M>? = null

    internal fun attachToPlayer(
        mediaPlayer: MediaPlayer<M>,
        playbackStateFactory: PlaybackStateFactory<M>
    ) {
        this.mediaPlayer = mediaPlayer
        this.playbackStateFactory = playbackStateFactory
        onAttached()
    }

    internal suspend fun interceptInitialPlayerState(
        pendingTransportState: TransportState<M>?
    ): TransportState<M>? {
        val modifiedState = onInterceptInitializationState(pendingTransportState)

        if (pendingTransportState != null && modifiedState != pendingTransportState) {
            if (modifiedState == null) {
                Log.w(
                    "Encore",
                    "Extension ${javaClass.name} has cleared the state that the player will " +
                        "initialize to."
                )
            } else {
                Log.i(
                    "Encore",
                    "Extension ${javaClass.name} modified the state that the player will " +
                        "initialize to."
                )
            }
        }

        return modifiedState
    }

    internal fun dispatchPlayerInitialized() {
        onPlayerFullyInitialized()
    }

    internal fun dispatchNewState(newState: MediaPlayerState.Initialized<M>) {
        onNewPlayerState(newState)
    }

    internal fun release() {
        onRelease()
        mediaPlayer = null
        playbackStateFactory = null
    }

    private fun requireMediaPlayer() = checkNotNull(mediaPlayer) {
        "This extension is not attached to a media player. " +
            "Did you call this before onPrepared() or after onRelease()?"
    }

    private fun requirePlaybackStateFactory() = checkNotNull(playbackStateFactory) {
        "This extension is not attached to a media player. " +
            "Did you call this before onPrepared() or after onRelease()?"
    }

    protected fun getCurrentPlaybackState(): MediaPlayerState<M> = requireMediaPlayer().getState()

    protected fun requireCurrentTransportState(): TransportState<M> {
        val playbackState = getCurrentPlaybackState()
        check(playbackState is MediaPlayerState.Initialized) {
            "Cannot read current transport state. The player is not initialized."
        }
        return playbackState.transportState
    }

    protected fun setTransportState(state: TransportState<M>) {
        requireMediaPlayer().setState(state)
    }

    protected inline fun modifyTransportState(
        modification: TransportState<M>.() -> TransportState<M>
    ) {
        setTransportState(requireCurrentTransportState().modification())
    }

    protected open fun onAttached() {

    }

    protected open suspend fun onInterceptInitializationState(
        pendingTransportState: TransportState<M>?
    ): TransportState<M>? {
        return pendingTransportState
    }

    protected open fun onPlayerFullyInitialized() {

    }

    protected abstract fun onNewPlayerState(newState: MediaPlayerState.Initialized<M>)

    protected open fun onRelease() {

    }

    protected fun TransportState<M>.play(): TransportState<M> =
        requirePlaybackStateFactory().play(this)

    protected fun TransportState<M>.pause(): TransportState<M> =
        requirePlaybackStateFactory().pause(this)

    protected fun TransportState<M>.seekTo(seekPositionMillis: Long): TransportState<M> =
        requirePlaybackStateFactory().seekTo(this, seekPositionMillis)

    protected fun TransportState<M>.skipToPrevious(): TransportState<M> =
        requirePlaybackStateFactory().skipToPrevious(this)

    protected fun TransportState<M>.skipToNext(): TransportState<M> =
        requirePlaybackStateFactory().skipToNext(this)

    protected fun TransportState<M>.skipToIndex(index: Int): TransportState<M> =
        requirePlaybackStateFactory().skipToIndex(this, index)

    protected fun TransportState<M>.setShuffleMode(shuffleMode: ShuffleMode): TransportState<M> =
        requirePlaybackStateFactory().setShuffleMode(this, shuffleMode)

    protected fun TransportState<M>.setRepeatMode(repeatMode: RepeatMode): TransportState<M> =
        requirePlaybackStateFactory().setRepeatMode(this, repeatMode)

}
