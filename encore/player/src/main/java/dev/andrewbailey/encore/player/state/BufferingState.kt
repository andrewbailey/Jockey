package dev.andrewbailey.encore.player.state

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

public sealed class BufferingState : Parcelable {

    @Parcelize
    public object Buffered : BufferingState()

    @Parcelize
    public data class Buffering(
        val pausedForBuffering: Boolean,
        val bufferedAmountMs: Int
    ) : BufferingState()

}
