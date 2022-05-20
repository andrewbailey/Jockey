package dev.andrewbailey.encore.player.playback

import android.util.Log
import dev.andrewbailey.encore.model.MediaObject
import dev.andrewbailey.encore.player.state.MediaPlaybackState
import dev.andrewbailey.encore.player.state.MediaPlayerState
import dev.andrewbailey.encore.player.state.RepeatMode
import dev.andrewbailey.encore.player.state.ShuffleMode
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
        pendingMediaPlaybackState: MediaPlaybackState<M>?
    ): MediaPlaybackState<M>? {
        val modifiedState = onInterceptInitializationState(pendingMediaPlaybackState)

        if (pendingMediaPlaybackState != null && modifiedState != pendingMediaPlaybackState) {
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

    protected fun requireCurrentTransportState(): MediaPlaybackState<M> {
        val playbackState = getCurrentPlaybackState()
        check(playbackState is MediaPlayerState.Initialized) {
            "Cannot read current transport state. The player is not initialized."
        }
        return playbackState.mediaPlaybackState
    }

    protected fun setTransportState(state: MediaPlaybackState<M>) {
        requireMediaPlayer().setState(state)
    }

    protected inline fun modifyTransportState(
        modification: MediaPlaybackState<M>.() -> MediaPlaybackState<M>
    ) {
        setTransportState(requireCurrentTransportState().modification())
    }

    protected open fun onAttached() {

    }

    protected open suspend fun onInterceptInitializationState(
        pendingMediaPlaybackState: MediaPlaybackState<M>?
    ): MediaPlaybackState<M>? {
        return pendingMediaPlaybackState
    }

    protected open fun onPlayerFullyInitialized() {

    }

    protected abstract fun onNewPlayerState(newState: MediaPlayerState.Initialized<M>)

    protected open fun onRelease() {

    }

    protected fun MediaPlaybackState<M>.play(): MediaPlaybackState<M> =
        requirePlaybackStateFactory().play(this)

    protected fun MediaPlaybackState<M>.pause(): MediaPlaybackState<M> =
        requirePlaybackStateFactory().pause(this)

    protected fun MediaPlaybackState<M>.seekTo(seekPositionMillis: Long): MediaPlaybackState<M> =
        requirePlaybackStateFactory().seekTo(this, seekPositionMillis)

    protected fun MediaPlaybackState<M>.skipToPrevious(): MediaPlaybackState<M> =
        requirePlaybackStateFactory().skipToPrevious(this)

    protected fun MediaPlaybackState<M>.skipToNext(): MediaPlaybackState<M> =
        requirePlaybackStateFactory().skipToNext(this)

    protected fun MediaPlaybackState<M>.skipToIndex(index: Int): MediaPlaybackState<M> =
        requirePlaybackStateFactory().skipToIndex(this, index)

    protected fun MediaPlaybackState<M>.setShuffleMode(
        shuffleMode: ShuffleMode
    ): MediaPlaybackState<M> = requirePlaybackStateFactory().setShuffleMode(this, shuffleMode)

    protected fun MediaPlaybackState<M>.setRepeatMode(
        repeatMode: RepeatMode
    ): MediaPlaybackState<M> = requirePlaybackStateFactory().setRepeatMode(this, repeatMode)

}
