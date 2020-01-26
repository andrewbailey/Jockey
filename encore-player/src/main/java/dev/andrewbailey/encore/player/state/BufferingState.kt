package dev.andrewbailey.encore.player.state

sealed class BufferingState {

    object Buffered : BufferingState()

    data class Buffering(
        val pausedForBuffering: Boolean,
        val bufferedAmountMs: Int
    ) : BufferingState()

}
