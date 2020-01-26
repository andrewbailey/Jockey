package dev.andrewbailey.encore.player.playback

import dev.andrewbailey.encore.player.state.MediaPlayerState

abstract class PlaybackObserver {

    abstract fun onPlaybackStateChanged(newState: MediaPlayerState)

    open fun onRelease() {

    }

}
