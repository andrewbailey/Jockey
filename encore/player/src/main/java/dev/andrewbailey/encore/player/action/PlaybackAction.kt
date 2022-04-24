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
    Play(ACTION_PLAY),
    Pause(ACTION_PAUSE),
    PlayPause(ACTION_PLAY_PAUSE),
    SkipPrevious(ACTION_SKIP_TO_PREVIOUS),
    SkipNext(ACTION_SKIP_TO_NEXT),
    Stop(ACTION_STOP)
}
