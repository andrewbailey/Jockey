package dev.andrewbailey.encore.player.playback

import dev.andrewbailey.encore.player.state.MediaPlayerState
import dev.andrewbailey.encore.player.state.RepeatMode
import dev.andrewbailey.encore.player.state.ShuffleMode
import dev.andrewbailey.encore.player.state.TransportState
import dev.andrewbailey.encore.player.state.factory.PlaybackStateFactory

public abstract class PlaybackExtension {

    private var mediaPlayer: MediaPlayer? = null
    private var playbackStateFactory: PlaybackStateFactory? = null

    internal fun initialize(
        mediaPlayer: MediaPlayer,
        playbackStateFactory: PlaybackStateFactory
    ) {
        this.mediaPlayer = mediaPlayer
        this.playbackStateFactory = playbackStateFactory
        onPrepared()
    }

    internal fun dispatchNewState(newState: MediaPlayerState) {
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

    protected fun getCurrentPlaybackState(): MediaPlayerState = requireMediaPlayer().getState()

    protected fun setTransportState(state: TransportState) {
        requireMediaPlayer().setState(state)
    }

    protected inline fun modifyTransportState(modification: TransportState.() -> TransportState) {
        setTransportState(getCurrentPlaybackState().transportState.modification())
    }

    protected open fun onPrepared() {

    }

    protected abstract fun onNewPlayerState(newState: MediaPlayerState)

    protected open fun onRelease() {

    }

    protected fun TransportState.play(): TransportState =
        requirePlaybackStateFactory().play(this)

    protected fun TransportState.pause(): TransportState =
        requirePlaybackStateFactory().pause(this)

    protected fun TransportState.seekTo(seekPositionMillis: Long): TransportState =
        requirePlaybackStateFactory().seekTo(this, seekPositionMillis)

    protected fun TransportState.skipToPrevious(): TransportState =
        requirePlaybackStateFactory().skipToPrevious(this)

    protected fun TransportState.skipToNext(): TransportState =
        requirePlaybackStateFactory().skipToNext(this)

    protected fun TransportState.skipToIndex(index: Int): TransportState =
        requirePlaybackStateFactory().skipToIndex(this, index)

    protected fun TransportState.setShuffleMode(shuffleMode: ShuffleMode): TransportState =
        requirePlaybackStateFactory().setShuffleMode(this, shuffleMode)

    protected fun TransportState.setRepeatMode(repeatMode: RepeatMode): TransportState =
        requirePlaybackStateFactory().setRepeatMode(this, repeatMode)

}
