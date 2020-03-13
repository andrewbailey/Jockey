package dev.andrewbailey.encore.player.state

import android.graphics.Bitmap
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

sealed class MediaPlayerState : Parcelable {

    abstract val transportState: TransportState

    @Parcelize
    class Prepared internal constructor(
        override val transportState: TransportState.Active,
        val artwork: Bitmap?,
        val durationMs: Long?,
        val bufferingState: BufferingState
    ) : MediaPlayerState()

    @Parcelize
    class Ready internal constructor(
        override val transportState: TransportState.Idle
    ) : MediaPlayerState()

}
