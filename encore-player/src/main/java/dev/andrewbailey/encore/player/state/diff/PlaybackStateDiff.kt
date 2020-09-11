package dev.andrewbailey.encore.player.state.diff

import dev.andrewbailey.encore.model.MediaItem

internal data class PlaybackStateDiff<M : MediaItem>(
    val operations: List<PlaybackStateModification<M>>
)
