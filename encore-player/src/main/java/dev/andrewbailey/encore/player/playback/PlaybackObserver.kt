package dev.andrewbailey.encore.player.playback

import dev.andrewbailey.encore.model.MediaObject
import dev.andrewbailey.encore.player.state.MediaPlayerState

public interface PlaybackObserver<M : MediaObject> {

    public fun onPlaybackStateChanged(newState: MediaPlayerState<M>)

    public fun onRelease() {

    }

}
