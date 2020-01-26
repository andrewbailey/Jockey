package dev.andrewbailey.encore.player.playback

import dev.andrewbailey.encore.player.state.MediaPlayerState
import dev.andrewbailey.encore.player.state.RepeatMode
import dev.andrewbailey.encore.player.state.ShuffleMode
import dev.andrewbailey.encore.player.state.TransportState
import dev.andrewbailey.encore.player.state.factory.PlaybackStateFactory

abstract class PlaybackExtension {

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

    protected fun getCurrentPlaybackState() = requireMediaPlayer().getState()

    protected fun setPlaybackState(state: TransportState) {
        requireMediaPlayer().setState(state)
    }

    protected inline fun modifyTransportState(modification: TransportState.() -> TransportState) {
        setPlaybackState(getCurrentPlaybackState().transportState.modification())
    }

    open fun onPrepared() {

    }

    abstract fun onNewPlayerState(newState: MediaPlayerState)

    open fun onRelease() {

    }

    protected fun TransportState.play() =
        requirePlaybackStateFactory().play(this)

    protected fun TransportState.pause() =
        requirePlaybackStateFactory().pause(this)

    protected fun TransportState.seekTo(seekPositionMillis: Long) =
        requirePlaybackStateFactory().seekTo(this, seekPositionMillis)

    protected fun TransportState.skipToPrevious() =
        requirePlaybackStateFactory().skipToPrevious(this)

    protected fun TransportState.skipToNext() =
        requirePlaybackStateFactory().skipToNext(this)

    protected fun TransportState.skipToIndex(index: Int) =
        requirePlaybackStateFactory().skipToIndex(this, index)

    protected fun TransportState.setShuffleMode(shuffleMode: ShuffleMode) =
        requirePlaybackStateFactory().setShuffleMode(this, shuffleMode)

    protected fun TransportState.setRepeatMode(repeatMode: RepeatMode) =
        requirePlaybackStateFactory().setRepeatMode(this, repeatMode)

}
