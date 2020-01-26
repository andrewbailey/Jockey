package dev.andrewbailey.encore.player.notification

import android.support.v4.media.session.PlaybackStateCompat.MediaKeyAction
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.marverenic.encore.player.action.PlaybackAction
import dev.andrewbailey.encore.player.action.CustomActionProvider
import dev.andrewbailey.encore.player.state.MediaPlayerState

sealed class NotificationAction {

    internal abstract fun getNotificationActionIcon(
        state: MediaPlayerState,
        customActionProviders: List<CustomActionProvider>
    ): NotificationActionIcon?

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

        inline fun <reified T : CustomActionProvider> fromCustomAction(
            showInCompactView: Boolean = true
        ) = fromCustomAction(
            customActionClass = T::class.java,
            showInCompactView = showInCompactView
        )

        fun <T : CustomActionProvider> fromCustomAction(
            customActionClass: Class<T>,
            showInCompactView: Boolean = true
        ): NotificationAction = CustomNotificationAction(
            customActionClass = customActionClass,
            showInCompactView = showInCompactView
        )
    }

}

internal class DefaultNotificationAction(
    @DrawableRes
    private val icon: Int,
    @StringRes
    private val title: Int,
    @MediaKeyAction
    private val mediaKeyAction: Long,
    private val showInCompactView: Boolean
) : NotificationAction() {

    override fun getNotificationActionIcon(
        state: MediaPlayerState,
        customActionProviders: List<CustomActionProvider>
    ) = DefaultNotificationActionIcon(
        icon = icon,
        title = title,
        mediaKeyAction = mediaKeyAction,
        showInCompactView = showInCompactView
    )

}

internal class CustomNotificationAction<T : CustomActionProvider>(
    private val customActionClass: Class<T>,
    private val showInCompactView: Boolean
) : NotificationAction() {

    override fun getNotificationActionIcon(
        state: MediaPlayerState,
        customActionProviders: List<CustomActionProvider>
    ): CustomNotificationActionIcon? {
        val provider = customActionProviders
            .firstOrNull { it.javaClass == customActionClass }

        if (provider == null) {
            Log.w("Encore", "Cannot show icon for ${customActionClass.name} in notification " +
                    "because the provider is not registered.  Did you override " +
                    "PlayerService.onCreateCustomActions()?")
        }

        val action = provider?.getAction(state) ?: return null

        require(action.icon != 0) {
            "Notification actions must have an icon"
        }

        require(action.name != 0) {
            "Notification actions must have a name"
        }

        return CustomNotificationActionIcon(
            icon = action.icon,
            title = action.name,
            actionId = action.id,
            showInCompactView = showInCompactView
        )
    }

}
