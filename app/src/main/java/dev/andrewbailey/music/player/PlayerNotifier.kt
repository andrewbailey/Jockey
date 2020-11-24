package dev.andrewbailey.music.player

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import dev.andrewbailey.encore.player.action.PlaybackAction
import dev.andrewbailey.encore.player.notification.NotificationAction
import dev.andrewbailey.encore.player.notification.NotificationProvider
import dev.andrewbailey.encore.player.state.MediaPlayerState
import dev.andrewbailey.encore.player.state.PlaybackState.PLAYING
import dev.andrewbailey.encore.player.state.TransportState.Active
import dev.andrewbailey.encore.player.state.TransportState.Idle
import dev.andrewbailey.encore.provider.mediastore.LocalSong
import dev.andrewbailey.music.R
import dev.andrewbailey.music.ui.MainActivity

class PlayerNotifier : NotificationProvider<LocalSong>(CHANNEL_ID) {

    companion object {
        const val CHANNEL_ID = "playback"
    }

    override fun getNotificationIcon(playbackState: MediaPlayerState<LocalSong>): Int {
        val state = playbackState.transportState
        return when {
            state is Active && state.status == PLAYING -> R.drawable.ic_notification_play
            else -> R.drawable.ic_notification_pause
        }
    }

    override fun getNotificationColor(
        context: Context,
        playbackState: MediaPlayerState<LocalSong>
    ) = ContextCompat.getColor(context, R.color.colorPrimary)

    override fun getContentIntent(
        context: Context,
        playbackState: MediaPlayerState<LocalSong>
    ) = PendingIntent.getActivity(context, 0, Intent(context, MainActivity::class.java), 0)

    override fun getActions(
        playbackState: MediaPlayerState<LocalSong>
    ): List<NotificationAction<LocalSong>> {
        return when (val transportState = playbackState.transportState) {
            is Idle -> emptyList()
            is Active -> listOf(
                NotificationAction.fromPlaybackAction(
                    icon = R.drawable.ic_skip_previous,
                    title = R.string.app_name,
                    action = PlaybackAction.SKIP_PREVIOUS
                ),
                if (transportState.status == PLAYING) {
                    NotificationAction.fromPlaybackAction(
                        icon = R.drawable.ic_pause,
                        title = R.string.app_name,
                        action = PlaybackAction.PAUSE
                    )
                } else {
                    NotificationAction.fromPlaybackAction(
                        icon = R.drawable.ic_play,
                        title = R.string.app_name,
                        action = PlaybackAction.PLAY
                    )
                },
                NotificationAction.fromPlaybackAction(
                    icon = R.drawable.ic_skip_next,
                    title = R.string.app_name,
                    action = PlaybackAction.SKIP_NEXT
                )
            )
        }
    }

}
