package dev.andrewbailey.encore.player.notification

import android.app.NotificationManager
import android.app.Service
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.content.getSystemService
import dev.andrewbailey.encore.player.action.CustomActionIntents
import dev.andrewbailey.encore.player.action.CustomActionProvider
import dev.andrewbailey.encore.player.action.QuitActionProvider
import dev.andrewbailey.encore.player.playback.PlaybackObserver
import dev.andrewbailey.encore.player.state.MediaPlayerState
import dev.andrewbailey.encore.player.state.PlaybackState
import dev.andrewbailey.encore.player.state.TransportState.Active

internal class PlaybackNotifier(
    private val service: Service,
    private val mediaSession: MediaSessionCompat,
    private val notificationId: Int,
    private val notificationProvider: NotificationProvider,
    private val customActionProviders: List<CustomActionProvider>
) : PlaybackObserver {

    private val notificationManager = service.getSystemService<NotificationManager>()
        ?: throw RuntimeException("Failed to get an instance of NotificationManager")

    init {
        require(notificationId != 0) {
            "notificationId cannot be zero"
        }
    }

    override fun onPlaybackStateChanged(newState: MediaPlayerState) {
        showNotification(
            foreground = (newState.transportState as? Active)?.status == PlaybackState.PLAYING,
            playbackState = newState
        )
    }

    fun showNotification(
        foreground: Boolean,
        playbackState: MediaPlayerState
    ) {
        val notification = notificationProvider.createNotification(
            service = service,
            foreground = foreground,
            playbackState = playbackState,
            customActionProviders = customActionProviders,
            mediaSession = mediaSession,
            stopIntent = CustomActionIntents.createIntent(service, QuitActionProvider.ACTION_ID)
        )

        if (foreground) {
            service.startForeground(notificationId, notification)
        } else {
            service.stopForeground(false)
            notificationManager.notify(notificationId, notification)
        }
    }

}
