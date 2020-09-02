package dev.andrewbailey.encore.player.playback

import dev.andrewbailey.encore.player.state.MediaPlayerState

public interface PlaybackObserver {

    public fun onPlaybackStateChanged(newState: MediaPlayerState)

    public fun onRelease() {

    }

}
