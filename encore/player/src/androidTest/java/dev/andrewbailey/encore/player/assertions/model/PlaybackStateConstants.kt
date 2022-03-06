package dev.andrewbailey.encore.player.assertions.model

import android.support.v4.media.session.PlaybackStateCompat

@Suppress("DEPRECATION")
object PlaybackStateConstants {
    enum class State(val flag: Int) {
        None(PlaybackStateCompat.STATE_NONE),
        Stopped(PlaybackStateCompat.STATE_STOPPED),
        Paused(PlaybackStateCompat.STATE_PAUSED),
        Playing(PlaybackStateCompat.STATE_PLAYING),
        FastForwarding(PlaybackStateCompat.STATE_FAST_FORWARDING),
        Rewinding(PlaybackStateCompat.STATE_REWINDING),
        Buffering(PlaybackStateCompat.STATE_BUFFERING),
        Error(PlaybackStateCompat.STATE_ERROR),
        Connecting(PlaybackStateCompat.STATE_CONNECTING),
        SkippingToPrevious(PlaybackStateCompat.STATE_SKIPPING_TO_PREVIOUS),
        SkippingToNext(PlaybackStateCompat.STATE_SKIPPING_TO_NEXT),
        SkippingToQueueItem(PlaybackStateCompat.STATE_SKIPPING_TO_QUEUE_ITEM);

        companion object {
            fun fromFlag(flag: Int) = values().find { it.flag == flag }
        }
    }

    enum class Action(val flag: Long) {
        Stop(PlaybackStateCompat.ACTION_STOP),
        Pause(PlaybackStateCompat.ACTION_PAUSE),
        Play(PlaybackStateCompat.ACTION_PLAY),
        Rewind(PlaybackStateCompat.ACTION_REWIND),
        SkipToPrevious(PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS),
        SkipToNext(PlaybackStateCompat.ACTION_SKIP_TO_NEXT),
        FastForward(PlaybackStateCompat.ACTION_FAST_FORWARD),
        SetRating(PlaybackStateCompat.ACTION_SET_RATING),
        SeekTo(PlaybackStateCompat.ACTION_SEEK_TO),
        PlayPause(PlaybackStateCompat.ACTION_PLAY_PAUSE),
        PlayFromMediaId(PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID),
        PlayFromSearch(PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH),
        SkipToQueueItem(PlaybackStateCompat.ACTION_SKIP_TO_QUEUE_ITEM),
        PlayFromUri(PlaybackStateCompat.ACTION_PLAY_FROM_URI),
        Prepare(PlaybackStateCompat.ACTION_PREPARE),
        PrepareFromMediaId(PlaybackStateCompat.ACTION_PREPARE_FROM_MEDIA_ID),
        PrepareFromSearch(PlaybackStateCompat.ACTION_PREPARE_FROM_SEARCH),
        PrepareFromUri(PlaybackStateCompat.ACTION_PREPARE_FROM_URI),
        SetRepeatMode(PlaybackStateCompat.ACTION_SET_REPEAT_MODE),
        SetShuffleModeEnabled(PlaybackStateCompat.ACTION_SET_SHUFFLE_MODE_ENABLED),
        SetCaptioningEnabled(PlaybackStateCompat.ACTION_SET_CAPTIONING_ENABLED),
        SetShuffleMode(PlaybackStateCompat.ACTION_SET_SHUFFLE_MODE),
        SetPlaybackSpeed(PlaybackStateCompat.ACTION_SET_PLAYBACK_SPEED);

        companion object {
            fun fromFlag(flag: Long) = values().find { it.flag == flag }

            fun fromPackedFlags(packedFlags: Long) = values()
                .filter { (packedFlags and it.flag) == it.flag }
        }
    }

    enum class RepeatMode(val flag: Int) {
        Invalid(PlaybackStateCompat.REPEAT_MODE_INVALID),
        None(PlaybackStateCompat.REPEAT_MODE_NONE),
        One(PlaybackStateCompat.REPEAT_MODE_ONE),
        All(PlaybackStateCompat.REPEAT_MODE_ALL),
        Group(PlaybackStateCompat.REPEAT_MODE_GROUP);

        companion object {
            fun fromFlag(flag: Int) = values().find { it.flag == flag }
        }
    }

    enum class ShuffleMode(val flag: Int) {
        Invalid(PlaybackStateCompat.SHUFFLE_MODE_INVALID),
        None(PlaybackStateCompat.SHUFFLE_MODE_NONE),
        All(PlaybackStateCompat.SHUFFLE_MODE_ALL),
        Group(PlaybackStateCompat.SHUFFLE_MODE_GROUP);

        companion object {
            fun fromFlag(flag: Int) = values().find { it.flag == flag }
        }
    }

    enum class ErrorCode(val flag: Int) {
        UnknownError(PlaybackStateCompat.ERROR_CODE_UNKNOWN_ERROR),
        AppError(PlaybackStateCompat.ERROR_CODE_APP_ERROR),
        NotSupported(PlaybackStateCompat.ERROR_CODE_NOT_SUPPORTED),
        AuthenticationExpired(PlaybackStateCompat.ERROR_CODE_AUTHENTICATION_EXPIRED),
        PremiumAccountRequired(PlaybackStateCompat.ERROR_CODE_PREMIUM_ACCOUNT_REQUIRED),
        ConcurrentStreamLimit(PlaybackStateCompat.ERROR_CODE_CONCURRENT_STREAM_LIMIT),
        ParentalControlRestricted(PlaybackStateCompat.ERROR_CODE_PARENTAL_CONTROL_RESTRICTED),
        NotAvailableInRegion(PlaybackStateCompat.ERROR_CODE_NOT_AVAILABLE_IN_REGION),
        ContentAlreadyPlaying(PlaybackStateCompat.ERROR_CODE_CONTENT_ALREADY_PLAYING),
        SkipLimitReached(PlaybackStateCompat.ERROR_CODE_SKIP_LIMIT_REACHED),
        ActionAborted(PlaybackStateCompat.ERROR_CODE_ACTION_ABORTED),
        EndOfQueue(PlaybackStateCompat.ERROR_CODE_END_OF_QUEUE);

        companion object {
            fun fromFlag(flag: Int) = values().find { it.flag == flag }
        }
    }
}
