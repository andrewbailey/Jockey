package dev.andrewbailey.encore.player.playback

import dev.andrewbailey.encore.player.state.PlaybackState
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

    protected fun getArtwork() = requireMediaPlayer().getArtwork()

    protected fun getCurrentPlaybackState() = requireMediaPlayer().getState()

    protected fun setPlaybackState(state: PlaybackState) = requireMediaPlayer().setState(state)

    protected inline fun modifyPlaybackState(modification: PlaybackState.() -> PlaybackState) {
        setPlaybackState(getCurrentPlaybackState().modification())
    }

    open fun onPrepared() {

    }

    open fun onNewPlayerState(newState: PlaybackState): PlaybackState {
        return newState
    }

    open fun onRelease() {

    }

    protected fun PlaybackState.play() =
        requirePlaybackStateFactory().play(this)

    protected fun PlaybackState.pause() =
        requirePlaybackStateFactory().pause(this)

    protected fun PlaybackState.seekTo(seekPositionMillis: Long) =
        requirePlaybackStateFactory().seekTo(this, seekPositionMillis)

    protected fun PlaybackState.skipToPrevious() =
        requirePlaybackStateFactory().skipToPrevious(this)

    protected fun PlaybackState.skipToNext() =
        requirePlaybackStateFactory().skipToNext(this)

    protected fun PlaybackState.skipToIndex(index: Int) =
        requirePlaybackStateFactory().skipToIndex(this, index)

}
