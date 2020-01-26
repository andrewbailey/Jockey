package dev.andrewbailey.encore.player.state.factory

import dev.andrewbailey.encore.player.state.RepeatMode
import dev.andrewbailey.encore.player.state.ShuffleMode
import dev.andrewbailey.encore.player.state.TransportState

abstract class PlaybackStateFactory {

    abstract fun play(state: TransportState): TransportState

    abstract fun pause(state: TransportState): TransportState

    abstract fun seekTo(state: TransportState, seekPositionMillis: Long): TransportState

    abstract fun skipToPrevious(state: TransportState): TransportState

    abstract fun skipToNext(state: TransportState): TransportState

    abstract fun skipToIndex(state: TransportState, index: Int): TransportState

    abstract fun setShuffleMode(state: TransportState, shuffleMode: ShuffleMode): TransportState

    abstract fun setRepeatMode(state: TransportState, repeatMode: RepeatMode): TransportState

}
