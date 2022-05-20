package dev.andrewbailey.encore.player.state.factory

import dev.andrewbailey.encore.model.MediaObject
import dev.andrewbailey.encore.model.MediaSearchArguments
import dev.andrewbailey.encore.player.state.MediaPlaybackState
import dev.andrewbailey.encore.player.state.RepeatMode
import dev.andrewbailey.encore.player.state.ShuffleMode
import dev.andrewbailey.encore.provider.MediaSearchResults

public abstract class PlaybackStateFactory<M : MediaObject> {

    public abstract fun play(
        state: MediaPlaybackState<M>
    ): MediaPlaybackState<M>

    public abstract fun pause(
        state: MediaPlaybackState<M>
    ): MediaPlaybackState<M>

    public abstract fun seekTo(
        state: MediaPlaybackState<M>,
        seekPositionMillis: Long
    ): MediaPlaybackState<M>

    public abstract fun skipToPrevious(
        state: MediaPlaybackState<M>
    ): MediaPlaybackState<M>

    public abstract fun skipToNext(
        state: MediaPlaybackState<M>
    ): MediaPlaybackState<M>

    public abstract fun skipToIndex(
        state: MediaPlaybackState<M>,
        index: Int
    ): MediaPlaybackState<M>

    public abstract fun setShuffleMode(
        state: MediaPlaybackState<M>,
        shuffleMode: ShuffleMode
    ): MediaPlaybackState<M>

    public abstract fun setRepeatMode(
        state: MediaPlaybackState<M>,
        repeatMode: RepeatMode
    ): MediaPlaybackState<M>

    public abstract fun playFromSearchResults(
        state: MediaPlaybackState<M>,
        query: String,
        beginPlayback: Boolean,
        arguments: MediaSearchArguments,
        searchResults: MediaSearchResults<M>
    ): MediaPlaybackState<M>

    public abstract fun playFromMediaBrowser(
        state: MediaPlaybackState<M>,
        browserId: String,
        mediaItemId: String,
        mediaItems: List<M>
    ): MediaPlaybackState<M>

}
