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

    return this is MediaPlayerState.Prepared && mediaPlaybackState.isPlaying()
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

    return this is MediaPlayerState.Prepared && mediaPlaybackState.isPaused()
}

public fun <M : MediaObject> MediaPlayerState<M>.isPausedOrHasNoContent(): Boolean {
    contract {
        returns(true) implies (this@isPausedOrHasNoContent is MediaPlayerState.Initialized)
    }

    return this is MediaPlayerState.Initialized && mediaPlaybackState.isPausedOrHasNoContent()
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

    return (this as? MediaPlayerState.Prepared)?.mediaPlaybackState?.queueStateOrNull()
}

public fun <M : MediaObject> MediaPlayerState.Prepared<M>.queueState(): QueueState<M> =
    mediaPlaybackState.queue

public fun <M : MediaObject> MediaPlayerState<M>.nowPlayingOrNull(): QueueItem<M>? {
    contract {
        returnsNotNull() implies (this@nowPlayingOrNull is MediaPlayerState.Prepared)
    }

    return (this as? MediaPlayerState.Prepared)?.mediaPlaybackState?.nowPlayingOrNull()
}

public fun <M : MediaObject> MediaPlayerState.Prepared<M>.nowPlaying(): QueueItem<M> =
    mediaPlaybackState.nowPlaying()

public fun <M : MediaObject> MediaPlayerState<M>.queueOrNull(): List<QueueItem<M>>? {
    contract {
        returnsNotNull() implies (this@queueOrNull is MediaPlayerState.Prepared)
    }

    return (this as? MediaPlayerState.Prepared)?.mediaPlaybackState?.queueOrNull()
}

public fun <M : MediaObject> MediaPlayerState<M>.queueIndexOrNull(): Int? {
    contract {
        returnsNotNull() implies (this@queueIndexOrNull is MediaPlayerState.Prepared)
    }

    return (this as? MediaPlayerState.Prepared)?.mediaPlaybackState?.queueIndexOrNull()
}

public fun <M : MediaObject> MediaPlayerState<M>.seekPositionOrNull(): SeekPosition? {
    contract {
        returnsNotNull() implies (this@seekPositionOrNull is MediaPlayerState.Prepared)
    }

    return (this as? MediaPlayerState.Prepared)?.mediaPlaybackState?.seekPositionOrNull()
}

public fun <M : MediaObject> MediaPlayerState<M>.seekPositionMillisOrNull(): Long? {
    contract {
        returnsNotNull() implies (this@seekPositionMillisOrNull is MediaPlayerState.Prepared)
    }

    return (this as? MediaPlayerState.Prepared)?.mediaPlaybackState?.seekPositionMillisOrNull()
}

public fun <M : MediaObject> MediaPlayerState<M>.durationMillisOrNull(): Long? {
    contract {
        returnsNotNull() implies (this@durationMillisOrNull is MediaPlayerState.Prepared)
    }

    return (this as? MediaPlayerState.Prepared)?.durationMs
}

public fun <M : MediaObject> MediaPlaybackState<M>.hasContent(): Boolean {
    contract {
        returns(true) implies (this@hasContent is MediaPlaybackState.Populated)
    }

    return this is MediaPlaybackState.Populated
}

public fun <M : MediaObject> MediaPlaybackState<M>.isPlaying(): Boolean {
    contract {
        returns(true) implies (this@isPlaying is MediaPlaybackState.Populated)
    }

    return this is MediaPlaybackState.Populated && status == Playing
}

public fun <M : MediaObject> MediaPlaybackState<M>.isPaused(): Boolean {
    contract {
        returns(true) implies (this@isPaused is MediaPlaybackState.Populated)
    }

    return this is MediaPlaybackState.Populated && status is Paused
}

public fun <M : MediaObject> MediaPlaybackState<M>.isPausedOrHasNoContent(): Boolean {
    return (this is MediaPlaybackState.Populated && status is Paused) ||
        this is MediaPlaybackState.Empty
}

public fun <M : MediaObject> MediaPlaybackState<M>.seekPositionOrNull(): SeekPosition? {
    contract {
        returnsNotNull() implies (this@seekPositionOrNull is MediaPlaybackState.Populated)
    }

    return (this as? MediaPlaybackState.Populated)?.seekPosition
}

public fun <M : MediaObject> MediaPlaybackState<M>.seekPositionMillisOrNull(): Long? {
    contract {
        returnsNotNull() implies (this@seekPositionMillisOrNull is MediaPlaybackState.Populated)
    }

    return seekPositionOrNull()?.seekPositionMillis
}

public fun <M : MediaObject> MediaPlaybackState<M>.nowPlayingOrNull(): QueueItem<M>? {
    contract {
        returnsNotNull() implies (this@nowPlayingOrNull is MediaPlaybackState.Populated)
    }

    return queueStateOrNull()?.let { it.queue[it.queueIndex] }
}

public fun <M : MediaObject> MediaPlaybackState.Populated<M>.nowPlaying(): QueueItem<M> =
    queue.queue[queue.queueIndex]

public fun <M : MediaObject> MediaPlaybackState<M>.queueStateOrNull(): QueueState<M>? {
    contract {
        returnsNotNull() implies (this@queueStateOrNull is MediaPlaybackState.Populated)
    }

    return (this as? MediaPlaybackState.Populated)?.queue
}

public fun <M : MediaObject> MediaPlaybackState<M>.queueOrNull(): List<QueueItem<M>>? {
    contract {
        returnsNotNull() implies (this@queueOrNull is MediaPlaybackState.Populated)
    }

    return queueStateOrNull()?.queue
}

public fun <M : MediaObject> MediaPlaybackState<M>.queueIndexOrNull(): Int? {
    contract {
        returnsNotNull() implies (this@queueIndexOrNull is MediaPlaybackState.Populated)
    }

    return queueStateOrNull()?.queueIndex
}

context(PlaybackStateFactory<M>)
public fun <M : MediaObject> MediaPlaybackState<M>.play(): MediaPlaybackState<M> =
    play(this)

context(PlaybackStateFactory<M>)
public fun <M : MediaObject> MediaPlaybackState<M>.pause(): MediaPlaybackState<M> =
    pause(this)

context(PlaybackStateFactory<M>)
public fun <M : MediaObject> MediaPlaybackState<M>.skipToPrevious(): MediaPlaybackState<M> =
    skipToPrevious(this)

context(PlaybackStateFactory<M>)
public fun <M : MediaObject> MediaPlaybackState<M>.skipToNext(): MediaPlaybackState<M> =
    skipToNext(this)

context(PlaybackStateFactory<M>)
public fun <M : MediaObject> MediaPlaybackState<M>.skipToIndex(
    index: Int
): MediaPlaybackState<M> = skipToIndex(this, index)

context(PlaybackStateFactory<M>)
public fun <M : MediaObject> MediaPlaybackState<M>.seekTo(
    seekPositionMillis: Long
): MediaPlaybackState<M> = seekTo(this, seekPositionMillis)

context(PlaybackStateFactory<M>)
public fun <M : MediaObject> MediaPlaybackState<M>.setShuffleMode(
    shuffleMode: ShuffleMode
): MediaPlaybackState<M> = setShuffleMode(this, shuffleMode)

context(PlaybackStateFactory<M>)
public fun <M : MediaObject> MediaPlaybackState<M>.setRepeatMode(
    repeatMode: RepeatMode
): MediaPlaybackState<M> = setRepeatMode(this, repeatMode)
