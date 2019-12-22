package dev.andrewbailey.encore.player.state.diff

internal data class PlaybackStateDiff(
    val operations: List<PlaybackStateModification>
)
