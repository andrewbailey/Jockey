@file:OptIn(ExperimentalContracts::class)
@file:Suppress("unused")

package dev.andrewbailey.encore.player.state

import android.graphics.Bitmap
import dev.andrewbailey.encore.model.MediaObject
import dev.andrewbailey.encore.model.QueueItem
import dev.andrewbailey.encore.player.state.PlaybackStatus.Paused
import dev.andrewbailey.encore.player.state.PlaybackStatus.Playing
import dev.andrewbailey.encore.player.state.factory.PlaybackStateFactory
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

public fun <M : MediaObject> MediaPlayerState<M>.hasContent(): Boolean {
    contract {
        returns(true) implies (this@hasContent is MediaPlayerState.Prepared)
    }

    return this is MediaPlayerState.Prepared
}

public fun <M : MediaObject> MediaPlayerState<M>.isPlaying(): Boolean {
    contract {
        returns(true) implies (this@isPlaying is MediaPlayerState.Prepared)
    }

    return this is MediaPlayerState.Prepared && transportState.isPlaying()
}

public fun <M : MediaObject> MediaPlayerState<M>.isPausedForBuffering(): Boolean {
    contract {
        returns(true) implies (this@isPausedForBuffering is MediaPlayerState.Prepared)
    }

    return this is MediaPlayerState.Prepared &&
        (bufferingState as? BufferingState.Buffering)?.pausedForBuffering == true
}

public fun <M : MediaObject> MediaPlayerState<M>.isPaused(): Boolean {
    contract {
        returns(true) implies (this@isPaused is MediaPlayerState.Prepared)
    }

    return this is MediaPlayerState.Prepared && transportState.isPaused()
}

public fun <M : MediaObject> MediaPlayerState<M>.isPausedOrHasNoContent(): Boolean {
    contract {
        returns(true) implies (this@isPausedOrHasNoContent is MediaPlayerState.Initialized)
    }

    return this is MediaPlayerState.Initialized && transportState.isPausedOrHasNoContent()
}

public fun <M : MediaObject> MediaPlayerState<M>.artworkOrNull(): Bitmap? {
    contract {
        returnsNotNull() implies (this@artworkOrNull is MediaPlayerState.Prepared)
    }

    return (this as? MediaPlayerState.Prepared)?.artwork
}

public fun <M : MediaObject> MediaPlayerState<M>.queueStateOrNull(): QueueState<M>? {
    contract {
        returnsNotNull() implies (this@queueStateOrNull is MediaPlayerState.Prepared)
    }

    return (this as? MediaPlayerState.Prepared)?.transportState?.queueStateOrNull()
}

public fun <M : MediaObject> MediaPlayerState.Prepared<M>.queueState(): QueueState<M> =
    transportState.queue

public fun <M : MediaObject> MediaPlayerState<M>.nowPlayingOrNull(): QueueItem<M>? {
    contract {
        returnsNotNull() implies (this@nowPlayingOrNull is MediaPlayerState.Prepared)
    }

    return (this as? MediaPlayerState.Prepared)?.transportState?.nowPlayingOrNull()
}

public fun <M : MediaObject> MediaPlayerState.Prepared<M>.nowPlaying(): QueueItem<M> =
    transportState.nowPlaying()

public fun <M : MediaObject> MediaPlayerState<M>.queueOrNull(): List<QueueItem<M>>? {
    contract {
        returnsNotNull() implies (this@queueOrNull is MediaPlayerState.Prepared)
    }

    return (this as? MediaPlayerState.Prepared)?.transportState?.queueOrNull()
}

public fun <M : MediaObject> MediaPlayerState<M>.queueIndexOrNull(): Int? {
    contract {
        returnsNotNull() implies (this@queueIndexOrNull is MediaPlayerState.Prepared)
    }

    return (this as? MediaPlayerState.Prepared)?.transportState?.queueIndexOrNull()
}

public fun <M : MediaObject> MediaPlayerState<M>.seekPositionOrNull(): SeekPosition? {
    contract {
        returnsNotNull() implies (this@seekPositionOrNull is MediaPlayerState.Prepared)
    }

    return (this as? MediaPlayerState.Prepared)?.transportState?.seekPositionOrNull()
}

public fun <M : MediaObject> MediaPlayerState<M>.seekPositionMillisOrNull(): Long? {
    contract {
        returnsNotNull() implies (this@seekPositionMillisOrNull is MediaPlayerState.Prepared)
    }

    return (this as? MediaPlayerState.Prepared)?.transportState?.seekPositionMillisOrNull()
}

public fun <M : MediaObject> MediaPlayerState<M>.durationMillisOrNull(): Long? {
    contract {
        returnsNotNull() implies (this@durationMillisOrNull is MediaPlayerState.Prepared)
    }

    return (this as? MediaPlayerState.Prepared)?.durationMs
}

public fun <M : MediaObject> TransportState<M>.hasContent(): Boolean {
    contract {
        returns(true) implies (this@hasContent is TransportState.Active)
    }

    return this is TransportState.Active
}

public fun <M : MediaObject> TransportState<M>.isPlaying(): Boolean {
    contract {
        returns(true) implies (this@isPlaying is TransportState.Active)
    }

    return this is TransportState.Active && status == Playing
}

public fun <M : MediaObject> TransportState<M>.isPaused(): Boolean {
    contract {
        returns(true) implies (this@isPaused is TransportState.Active)
    }

    return this is TransportState.Active && status is Paused
}

public fun <M : MediaObject> TransportState<M>.isPausedOrHasNoContent(): Boolean {
    return (this is TransportState.Active && status is Paused) || this is TransportState.Idle
}

public fun <M : MediaObject> TransportState<M>.seekPositionOrNull(): SeekPosition? {
    contract {
        returnsNotNull() implies (this@seekPositionOrNull is TransportState.Active)
    }

    return (this as? TransportState.Active)?.seekPosition
}

public fun <M : MediaObject> TransportState<M>.seekPositionMillisOrNull(): Long? {
    contract {
        returnsNotNull() implies (this@seekPositionMillisOrNull is TransportState.Active)
    }

    return seekPositionOrNull()?.seekPositionMillis
}

public fun <M : MediaObject> TransportState<M>.nowPlayingOrNull(): QueueItem<M>? {
    contract {
        returnsNotNull() implies (this@nowPlayingOrNull is TransportState.Active)
    }

    return queueStateOrNull()?.let { it.queue[it.queueIndex] }
}

public fun <M : MediaObject> TransportState.Active<M>.nowPlaying(): QueueItem<M> =
    queue.queue[queue.queueIndex]

public fun <M : MediaObject> TransportState<M>.queueStateOrNull(): QueueState<M>? {
    contract {
        returnsNotNull() implies (this@queueStateOrNull is TransportState.Active)
    }

    return (this as? TransportState.Active)?.queue
}

public fun <M : MediaObject> TransportState<M>.queueOrNull(): List<QueueItem<M>>? {
    contract {
        returnsNotNull() implies (this@queueOrNull is TransportState.Active)
    }

    return queueStateOrNull()?.queue
}

public fun <M : MediaObject> TransportState<M>.queueIndexOrNull(): Int? {
    contract {
        returnsNotNull() implies (this@queueIndexOrNull is TransportState.Active)
    }

    return queueStateOrNull()?.queueIndex
}

context(PlaybackStateFactory<M>)
public fun <M : MediaObject> TransportState<M>.play(): TransportState<M> =
    play(this)

context(PlaybackStateFactory<M>)
public fun <M : MediaObject> TransportState<M>.pause(): TransportState<M> =
    pause(this)

context(PlaybackStateFactory<M>)
public fun <M : MediaObject> TransportState<M>.skipToPrevious(): TransportState<M> =
    skipToPrevious(this)

context(PlaybackStateFactory<M>)
public fun <M : MediaObject> TransportState<M>.skipToNext(): TransportState<M> =
    skipToNext(this)

context(PlaybackStateFactory<M>)
public fun <M : MediaObject> TransportState<M>.skipToIndex(
    index: Int
): TransportState<M> = skipToIndex(this, index)

context(PlaybackStateFactory<M>)
public fun <M : MediaObject> TransportState<M>.seekTo(
    seekPositionMillis: Long
): TransportState<M> = seekTo(this, seekPositionMillis)

context(PlaybackStateFactory<M>)
public fun <M : MediaObject> TransportState<M>.setShuffleMode(
    shuffleMode: ShuffleMode
): TransportState<M> = setShuffleMode(this, shuffleMode)

context(PlaybackStateFactory<M>)
public fun <M : MediaObject> TransportState<M>.setRepeatMode(
    repeatMode: RepeatMode
): TransportState<M> = setRepeatMode(this, repeatMode)
