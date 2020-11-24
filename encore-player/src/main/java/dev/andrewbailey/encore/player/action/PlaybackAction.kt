package dev.andrewbailey.encore.player.action

import android.support.v4.media.session.PlaybackStateCompat.ACTION_PAUSE
import android.support.v4.media.session.PlaybackStateCompat.ACTION_PLAY
import android.support.v4.media.session.PlaybackStateCompat.ACTION_PLAY_PAUSE
import android.support.v4.media.session.PlaybackStateCompat.ACTION_SKIP_TO_NEXT
import android.support.v4.media.session.PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
import android.support.v4.media.session.PlaybackStateCompat.ACTION_STOP
import android.support.v4.media.session.PlaybackStateCompat.MediaKeyAction

@Suppress("unused")
public enum class PlaybackAction(
    @MediaKeyAction
    internal val mediaKeyAction: Long
) {
    PLAY(ACTION_PLAY),
    PAUSE(ACTION_PAUSE),
    PLAY_PAUSE(ACTION_PLAY_PAUSE),
    SKIP_PREVIOUS(ACTION_SKIP_TO_PREVIOUS),
    SKIP_NEXT(ACTION_SKIP_TO_NEXT),
    STOP(ACTION_STOP)
}
