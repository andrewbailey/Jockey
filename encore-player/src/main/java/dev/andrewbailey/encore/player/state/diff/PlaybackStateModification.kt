package dev.andrewbailey.encore.player.state.diff

import dev.andrewbailey.encore.model.QueueItem

internal sealed class PlaybackStateModification

internal data class TimelinePositionChange(
    val queueIndex: Int,
    val seekPositionMillis: Long
) : PlaybackStateModification()

internal data class SetPlaying(
    val isPlaying: Boolean
) : PlaybackStateModification()

internal object StopPlayback : PlaybackStateModification()

// TODO Break this model into atomic operations
internal data class QueueModification(
    val newQueue: List<QueueItem>
) : PlaybackStateModification()
