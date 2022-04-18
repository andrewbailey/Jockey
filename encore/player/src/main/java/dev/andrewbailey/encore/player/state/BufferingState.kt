package dev.andrewbailey.encore.player.state

import android.os.Parcelable
import dev.andrewbailey.annotations.compose.ComposeStableClass
import kotlinx.parcelize.Parcelize

@ComposeStableClass
public sealed class BufferingState : Parcelable {

    @ComposeStableClass
    @Parcelize
    public object Buffered : BufferingState()

    @ComposeStableClass
    @Parcelize
    public data class Buffering(
        val pausedForBuffering: Boolean,
        val bufferedAmountMs: Int
    ) : BufferingState()

}
