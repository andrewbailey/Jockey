package dev.andrewbailey.encore.player.state.diff

import dev.andrewbailey.encore.player.playback.MediaQueueItems
import dev.andrewbailey.encore.player.state.RepeatMode

internal sealed class PlaybackStateModification

internal data class TimelinePositionChange(
    val queueIndex: Int,
    val seekPositionMillis: Long
) : PlaybackStateModification()

internal data class SetPlaying(
    val isPlaying: Boolean
) : PlaybackStateModification()

internal data class SetRepeatMode(
    val repeatMode: RepeatMode
) : PlaybackStateModification()

internal object StopPlayback : PlaybackStateModification()

internal data class QueueModification(
    val updatedQueue: MediaQueueItems?,
    val isSeamless: Boolean
) : PlaybackStateModification()
