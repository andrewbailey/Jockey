package dev.andrewbailey.encore.player.notification

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.support.v4.media.session.MediaSessionCompat
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.core.app.NotificationCompat
import androidx.media.app.NotificationCompat.MediaStyle
import dev.andrewbailey.encore.player.state.PlaybackState

abstract class NotificationProvider(
    private val notificationChannelId: String
) {

    internal fun createNotification(
        context: Context,
        playbackState: PlaybackState,
        mediaSession: MediaSessionCompat,
        stopIntent: PendingIntent
    ): Notification {
        return NotificationCompat.Builder(context, notificationChannelId)
            .setStyle(
                MediaStyle()
                    .setMediaSession(mediaSession.sessionToken)
                    .setShowCancelButton(true)
                    .setCancelButtonIntent(stopIntent)
            )
            .setContentTitle(mediaSession.controller.metadata?.description?.title)
            .setContentText(mediaSession.controller.metadata?.description?.subtitle)
            .setLargeIcon(mediaSession.controller.metadata?.description?.iconBitmap)
            .setSmallIcon(getNotificationIcon(playbackState))
            .setColor(getNotificationColor(context, playbackState))
            .setContentIntent(getContentIntent(context, playbackState))
            .setDeleteIntent(stopIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setWhen(0)
            .setShowWhen(false)
            .build()
    }

    @DrawableRes
    abstract fun getNotificationIcon(playbackState: PlaybackState): Int

    @ColorInt
    open fun getNotificationColor(context: Context, playbackState: PlaybackState): Int {
        return NotificationCompat.COLOR_DEFAULT
    }

    abstract fun getContentIntent(context: Context, playbackState: PlaybackState): PendingIntent

}