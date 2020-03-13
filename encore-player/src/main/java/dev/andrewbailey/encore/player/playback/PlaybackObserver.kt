package dev.andrewbailey.encore.player.playback

import dev.andrewbailey.encore.player.state.MediaPlayerState

interface PlaybackObserver {

    fun onPlaybackStateChanged(newState: MediaPlayerState)

    fun onRelease() {

    }

}
