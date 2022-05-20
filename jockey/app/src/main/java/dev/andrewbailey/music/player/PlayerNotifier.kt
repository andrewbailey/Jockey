package dev.andrewbailey.music.player

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.app.PendingIntent.getActivity
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import dev.andrewbailey.encore.player.action.PlaybackAction
import dev.andrewbailey.encore.player.notification.NotificationAction
import dev.andrewbailey.encore.player.notification.NotificationProvider
import dev.andrewbailey.encore.player.state.MediaPlayerState
import dev.andrewbailey.encore.player.state.hasContent
import dev.andrewbailey.encore.player.state.isPlaying
import dev.andrewbailey.music.R
import dev.andrewbailey.music.model.Song
import dev.andrewbailey.music.ui.MainActivity

class PlayerNotifier : NotificationProvider<Song>(CHANNEL_ID) {

    companion object {
        const val CHANNEL_ID = "playback"
    }

    override fun getNotificationIcon(playbackState: MediaPlayerState<Song>): Int {
        val state = playbackState.transportState
        return when {
            state?.isPlaying() == true -> R.drawable.ic_notification_play
            else -> R.drawable.ic_notification_pause
        }
    }

    override fun getNotificationColor(
        context: Context,
        playbackState: MediaPlayerState<Song>
    ) = ContextCompat.getColor(context, R.color.colorPrimary)

    @SuppressLint("InlinedApi")
    override fun getContentIntent(
        context: Context,
        playbackState: MediaPlayerState<Song>
    ): PendingIntent {
        val intent = Intent(context, MainActivity::class.java)
        return getActivity(context, 0, intent, FLAG_IMMUTABLE or FLAG_UPDATE_CURRENT)
    }

    override fun getActions(
        playbackState: MediaPlayerState.Initialized<Song>
    ): List<NotificationAction<Song>> {
        return when {
            playbackState.hasContent() -> listOf(
                NotificationAction.fromPlaybackAction(
                    icon = R.drawable.ic_skip_previous,
                    title = R.string.app_name,
                    action = PlaybackAction.SkipPrevious
                ),
                if (playbackState.isPlaying()) {
                    NotificationAction.fromPlaybackAction(
                        icon = R.drawable.ic_pause,
                        title = R.string.app_name,
                        action = PlaybackAction.Pause
                    )
                } else {
                    NotificationAction.fromPlaybackAction(
                        icon = R.drawable.ic_play,
                        title = R.string.app_name,
                        action = PlaybackAction.Play
                    )
                },
                NotificationAction.fromPlaybackAction(
                    icon = R.drawable.ic_skip_next,
                    title = R.string.app_name,
                    action = PlaybackAction.SkipNext
                )
            )
            else -> emptyList()
        }
    }

}
