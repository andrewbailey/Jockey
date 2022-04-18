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
import dev.andrewbailey.encore.model.MediaObject
import dev.andrewbailey.encore.player.action.CustomActionIntents
import dev.andrewbailey.encore.player.action.CustomActionProvider
import dev.andrewbailey.encore.player.state.MediaPlayerState

public abstract class NotificationProvider<M : MediaObject>(
    private val notificationChannelId: String
) {

    internal fun createNotification(
        service: Service,
        foreground: Boolean,
        playbackState: MediaPlayerState<M>,
        customActionProviders: List<CustomActionProvider<M>>,
        mediaSession: MediaSessionCompat,
        stopIntent: PendingIntent
    ): Notification {
        val actions = (playbackState as? MediaPlayerState.Initialized)
            ?.let { getActions(it) }
            .orEmpty()
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
            .setOngoing(foreground)
            .build()
    }

    @DrawableRes
    public abstract fun getNotificationIcon(
        playbackState: MediaPlayerState<M>
    ): Int

    @ColorInt
    public open fun getNotificationColor(
        context: Context,
        playbackState: MediaPlayerState<M>
    ): Int {
        return NotificationCompat.COLOR_DEFAULT
    }

    public abstract fun getContentIntent(
        context: Context,
        playbackState: MediaPlayerState<M>
    ): PendingIntent

    public abstract fun getActions(
        playbackState: MediaPlayerState.Initialized<M>
    ): List<NotificationAction<M>>

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
