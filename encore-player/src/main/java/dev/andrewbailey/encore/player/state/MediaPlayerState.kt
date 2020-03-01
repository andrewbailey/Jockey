package dev.andrewbailey.encore.player.state

import android.graphics.Bitmap

sealed class MediaPlayerState {

    abstract val transportState: TransportState

    class Prepared internal constructor(
        override val transportState: TransportState.Active,
        val artwork: Bitmap?,
        val durationMs: Long?,
        val bufferingState: BufferingState
    ) : MediaPlayerState()

    class Ready internal constructor(
        override val transportState: TransportState.Idle
    ) : MediaPlayerState()

}
