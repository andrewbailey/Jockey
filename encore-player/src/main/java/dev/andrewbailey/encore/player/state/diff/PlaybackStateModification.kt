package dev.andrewbailey.encore.player.state.diff

import dev.andrewbailey.encore.model.MediaItem
import dev.andrewbailey.encore.player.playback.MediaQueueItems
import dev.andrewbailey.encore.player.state.RepeatMode

internal sealed class PlaybackStateModification<out M : MediaItem>

internal data class TimelinePositionChange(
    val queueIndex: Int,
    val seekPositionMillis: Long
) : PlaybackStateModification<Nothing>()

internal data class SetPlaying(
    val isPlaying: Boolean
) : PlaybackStateModification<Nothing>()

internal data class SetRepeatMode(
    val repeatMode: RepeatMode
) : PlaybackStateModification<Nothing>()

internal object StopPlayback : PlaybackStateModification<Nothing>()

internal data class QueueModification<M : MediaItem>(
    val updatedQueue: MediaQueueItems<M>?,
    val isSeamless: Boolean
) : PlaybackStateModification<M>()
