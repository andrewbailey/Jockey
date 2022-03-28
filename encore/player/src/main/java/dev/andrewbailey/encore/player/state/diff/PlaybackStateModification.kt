package dev.andrewbailey.encore.player.state.diff

import dev.andrewbailey.encore.model.MediaObject
import dev.andrewbailey.encore.player.playback.MediaQueueItems
import dev.andrewbailey.encore.player.state.RepeatMode

internal sealed class PlaybackStateModification<out M : MediaObject>

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

internal data class SetPlaybackSpeed(
    val speed: Float,
    val pitch: Float
) : PlaybackStateModification<Nothing>()

internal object StopPlayback : PlaybackStateModification<Nothing>()

internal data class QueueModification<M : MediaObject>(
    val updatedQueue: MediaQueueItems<M>?,
    val isSeamless: Boolean
) : PlaybackStateModification<M>()
