package dev.andrewbailey.encore.player.state.factory

import dev.andrewbailey.encore.player.state.PlaybackState

abstract class PlaybackStateFactory {

    abstract fun play(state: PlaybackState): PlaybackState

    abstract fun pause(state: PlaybackState): PlaybackState

    abstract fun seekTo(state: PlaybackState, seekPositionMillis: Long): PlaybackState

    abstract fun skipToPrevious(state: PlaybackState): PlaybackState

    abstract fun skipToNext(state: PlaybackState): PlaybackState

    abstract fun skipToIndex(state: PlaybackState, index: Int): PlaybackState

}
