package dev.andrewbailey.encore.player.state.diff

import dev.andrewbailey.encore.model.MediaObject

internal data class PlaybackStateDiff<M : MediaObject>(
    val operations: List<PlaybackStateModification<M>>
)
