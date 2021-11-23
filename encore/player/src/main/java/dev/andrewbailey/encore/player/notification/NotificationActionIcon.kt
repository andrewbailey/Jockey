package dev.andrewbailey.encore.player.notification

import android.support.v4.media.session.PlaybackStateCompat.MediaKeyAction
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

internal sealed class NotificationActionIcon {

    @get:DrawableRes
    abstract val icon: Int

    @get:StringRes
    abstract val title: Int

    abstract val showInCompactView: Boolean

}

internal data class DefaultNotificationActionIcon(
    @DrawableRes
    override val icon: Int,
    @StringRes
    override val title: Int,
    @MediaKeyAction
    val mediaKeyAction: Long,
    override val showInCompactView: Boolean
) : NotificationActionIcon()

internal data class CustomNotificationActionIcon(
    @DrawableRes
    override val icon: Int,
    @StringRes
    override val title: Int,
    val actionId: String,
    override val showInCompactView: Boolean
) : NotificationActionIcon()
