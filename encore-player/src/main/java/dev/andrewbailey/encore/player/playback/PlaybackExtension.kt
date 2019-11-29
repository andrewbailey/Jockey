package dev.andrewbailey.encore.player.playback

import dev.andrewbailey.encore.player.state.PlaybackState

abstract class PlaybackExtension {

    private var mediaPlayer: MediaPlayer? = null

    internal fun initialize(mediaPlayer: MediaPlayer) {
        this.mediaPlayer = mediaPlayer
        onPrepared()
    }

    internal fun release() {
        onRelease()
        mediaPlayer = null
    }

    private fun requireMediaPlayer() = checkNotNull(mediaPlayer) {
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

}
