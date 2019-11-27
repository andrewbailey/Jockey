package dev.andrewbailey.encore.player.playback

import dev.andrewbailey.encore.player.state.PlaybackState

abstract class PlaybackObserver {

    abstract fun onPlaybackStateChanged(newState: PlaybackState)

    open fun onRelease() {

    }

}
