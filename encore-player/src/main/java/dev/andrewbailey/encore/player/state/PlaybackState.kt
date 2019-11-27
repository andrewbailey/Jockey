package dev.andrewbailey.encore.player.state

data class PlaybackState(
    val transportState: TransportState,
    val repeatMode: RepeatMode,
    val shuffleMode: ShuffleMode
)
