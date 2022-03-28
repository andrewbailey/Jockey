package dev.andrewbailey.encore.player.state.factory

import dev.andrewbailey.encore.model.MediaObject
import dev.andrewbailey.encore.model.MediaSearchArguments
import dev.andrewbailey.encore.player.state.RepeatMode
import dev.andrewbailey.encore.player.state.ShuffleMode
import dev.andrewbailey.encore.player.state.TransportState
import dev.andrewbailey.encore.provider.MediaSearchResults

public abstract class PlaybackStateFactory<M : MediaObject> {

    public abstract fun play(
        state: TransportState<M>
    ): TransportState<M>

    public abstract fun pause(
        state: TransportState<M>
    ): TransportState<M>

    public abstract fun seekTo(
        state: TransportState<M>,
        seekPositionMillis: Long
    ): TransportState<M>

    public abstract fun skipToPrevious(
        state: TransportState<M>
    ): TransportState<M>

    public abstract fun skipToNext(
        state: TransportState<M>
    ): TransportState<M>

    public abstract fun skipToIndex(
        state: TransportState<M>,
        index: Int
    ): TransportState<M>

    public abstract fun setShuffleMode(
        state: TransportState<M>,
        shuffleMode: ShuffleMode
    ): TransportState<M>

    public abstract fun setRepeatMode(
        state: TransportState<M>,
        repeatMode: RepeatMode
    ): TransportState<M>

    public abstract fun playFromSearchResults(
        state: TransportState<M>,
        query: String,
        beginPlayback: Boolean,
        arguments: MediaSearchArguments,
        searchResults: MediaSearchResults<M>
    ): TransportState<M>

    public abstract fun playFromMediaBrowser(
        state: TransportState<M>,
        browserId: String,
        mediaItemId: String,
        mediaItems: List<M>
    ): TransportState<M>

}
