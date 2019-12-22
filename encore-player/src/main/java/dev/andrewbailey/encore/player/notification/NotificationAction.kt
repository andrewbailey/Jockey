package dev.andrewbailey.encore.player.notification

import android.support.v4.media.session.PlaybackStateCompat.MediaKeyAction
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.marverenic.encore.player.action.PlaybackAction

sealed class NotificationAction {

    @get:DrawableRes
    abstract val icon: Int

    @get:StringRes
    abstract val title: Int

    abstract val showInCompactView: Boolean

    companion object {
        fun fromPlaybackAction(
            @DrawableRes icon: Int,
            @StringRes title: Int,
            action: PlaybackAction,
            showInCompactView: Boolean = true
        ): NotificationAction = DefaultNotificationAction(
            icon = icon,
            title = title,
            mediaKeyAction = action.mediaKeyAction,
            showInCompactView = showInCompactView
        )
    }

}

internal data class DefaultNotificationAction(
    @DrawableRes
    override val icon: Int,
    @StringRes
    override val title: Int,
    @MediaKeyAction
    val mediaKeyAction: Long,
    override val showInCompactView: Boolean
) : NotificationAction()
