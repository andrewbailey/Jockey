package dev.andrewbailey.encore.player

import android.app.Service
import android.content.Intent
import android.os.IBinder
import dev.andrewbailey.encore.player.notification.NotificationProvider
import dev.andrewbailey.encore.player.notification.PlaybackNotifier
import dev.andrewbailey.encore.player.os.MediaSessionController
import dev.andrewbailey.encore.player.playback.MediaPlayer
import dev.andrewbailey.encore.player.playback.PlaybackExtension
import dev.andrewbailey.encore.player.playback.PlaybackObserver

abstract class MediaPlayerService(
    tag: String,
    notificationId: Int,
    notificationProvider: NotificationProvider,
    extensions: List<PlaybackExtension> = emptyList(),
    observers: List<PlaybackObserver> = emptyList()
) : Service() {

    private val mediaSessionController by lazy {
        MediaSessionController(this, tag)
    }

    private val notifier by lazy {
        PlaybackNotifier(
            service = this,
            mediaSession = mediaSessionController.mediaSession,
            notificationId = notificationId,
            notificationProvider = notificationProvider
        )
    }

    private val mediaPlayer by lazy {
        MediaPlayer(
            context = applicationContext,
            extensions = listOf(
                mediaSessionController,
                *extensions.toTypedArray()
            ),
            observers = listOf(
                notifier,
                *observers.toTypedArray()
            )
        )
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
    }
}
