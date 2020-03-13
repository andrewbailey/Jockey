package dev.andrewbailey.encore.player.state

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

sealed class BufferingState : Parcelable {

    @Parcelize
    object Buffered : BufferingState()

    @Parcelize
    data class Buffering(
        val pausedForBuffering: Boolean,
        val bufferedAmountMs: Int
    ) : BufferingState()

}
