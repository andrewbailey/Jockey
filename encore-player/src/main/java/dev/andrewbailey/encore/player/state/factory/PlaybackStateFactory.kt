package dev.andrewbailey.encore.player.state.factory

import dev.andrewbailey.encore.model.MediaItem
import dev.andrewbailey.encore.player.state.RepeatMode
import dev.andrewbailey.encore.player.state.ShuffleMode
import dev.andrewbailey.encore.player.state.TransportState

public abstract class PlaybackStateFactory<M : MediaItem> {

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

}
