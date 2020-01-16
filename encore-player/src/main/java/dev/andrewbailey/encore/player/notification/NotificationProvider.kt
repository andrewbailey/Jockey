package dev.andrewbailey.encore.player.notification

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.support.v4.media.session.MediaSessionCompat
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.core.app.NotificationCompat
import androidx.media.app.NotificationCompat.MediaStyle
import androidx.media.session.MediaButtonReceiver
import dev.andrewbailey.encore.player.action.CustomActionIntents
import dev.andrewbailey.encore.player.action.CustomActionProvider
import dev.andrewbailey.encore.player.state.PlaybackState

abstract class NotificationProvider(
    private val notificationChannelId: String
) {

    internal fun createNotification(
        service: Service,
        playbackState: PlaybackState,
        customActionProviders: List<CustomActionProvider>,
        mediaSession: MediaSessionCompat,
        stopIntent: PendingIntent
    ): Notification {
        val actions = getActions(playbackState)
            .mapNotNull { action ->
                action.getNotificationActionIcon(
                    state = playbackState,
                    customActionProviders = customActionProviders
                )
            }

        return NotificationCompat.Builder(service, notificationChannelId)
            .setStyle(
                MediaStyle()
                    .setMediaSession(mediaSession.sessionToken)
                    .setShowCancelButton(true)
                    .setCancelButtonIntent(stopIntent)
                    .setShowActionsInCompactView(
                        *actions.mapIndexedNotNull { index, action ->
                            index.takeIf { action.showInCompactView }
                        }.toIntArray()
                    )
            )
            .setContentTitle(mediaSession.controller.metadata?.description?.title)
            .setContentText(mediaSession.controller.metadata?.description?.subtitle)
            .setLargeIcon(mediaSession.controller.metadata?.description?.iconBitmap)
            .setSmallIcon(getNotificationIcon(playbackState))
            .setColor(getNotificationColor(service, playbackState))
            .apply {
                actions.forEach { action ->
                    addAction(
                        action.icon,
                        service.getString(action.title),
                        getPendingIntent(service, action)
                    )
                }
            }
            .setContentIntent(getContentIntent(service, playbackState))
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

    abstract fun getActions(playbackState: PlaybackState): List<NotificationAction>

    private fun getPendingIntent(
        service: Service,
        action: NotificationActionIcon
    ): PendingIntent {
        return when (action) {
            is DefaultNotificationActionIcon -> {
                MediaButtonReceiver.buildMediaButtonPendingIntent(service, action.mediaKeyAction)
            }
            is CustomNotificationActionIcon -> {
                CustomActionIntents.createIntent(service, action.actionId)
            }
        }
    }

}
