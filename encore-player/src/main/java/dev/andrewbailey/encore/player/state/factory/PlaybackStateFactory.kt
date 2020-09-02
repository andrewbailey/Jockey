package dev.andrewbailey.encore.player.state.factory

import dev.andrewbailey.encore.player.state.RepeatMode
import dev.andrewbailey.encore.player.state.ShuffleMode
import dev.andrewbailey.encore.player.state.TransportState

public abstract class PlaybackStateFactory {

    public abstract fun play(
        state: TransportState
    ): TransportState

    public abstract fun pause(
        state: TransportState
    ): TransportState

    public abstract fun seekTo(
        state: TransportState,
        seekPositionMillis: Long
    ): TransportState

    public abstract fun skipToPrevious(
        state: TransportState
    ): TransportState

    public abstract fun skipToNext(
        state: TransportState
    ): TransportState

    public abstract fun skipToIndex(
        state: TransportState,
        index: Int
    ): TransportState

    public abstract fun setShuffleMode(
        state: TransportState,
        shuffleMode: ShuffleMode
    ): TransportState

    public abstract fun setRepeatMode(
        state: TransportState,
        repeatMode: RepeatMode
    ): TransportState

}
