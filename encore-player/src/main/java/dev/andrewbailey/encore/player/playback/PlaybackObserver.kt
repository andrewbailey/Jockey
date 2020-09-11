package dev.andrewbailey.encore.player.playback

import dev.andrewbailey.encore.model.MediaItem
import dev.andrewbailey.encore.player.state.MediaPlayerState

public interface PlaybackObserver<M : MediaItem> {

    public fun onPlaybackStateChanged(newState: MediaPlayerState<M>)

    public fun onRelease() {

    }

}
