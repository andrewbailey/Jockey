package dev.andrewbailey.encore.player.notification

import android.support.v4.media.session.PlaybackStateCompat.MediaKeyAction
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.marverenic.encore.player.action.PlaybackAction
import dev.andrewbailey.encore.model.MediaItem
import dev.andrewbailey.encore.player.action.CustomActionProvider
import dev.andrewbailey.encore.player.state.MediaPlayerState

public sealed class NotificationAction<M : MediaItem> {

    internal abstract fun getNotificationActionIcon(
        state: MediaPlayerState<M>,
        customActionProviders: List<CustomActionProvider<M>>
    ): NotificationActionIcon?

    public companion object {
        public fun <M : MediaItem> fromPlaybackAction(
            @DrawableRes icon: Int,
            @StringRes title: Int,
            action: PlaybackAction,
            showInCompactView: Boolean = true
        ): NotificationAction<M> = DefaultNotificationAction(
            icon = icon,
            title = title,
            mediaKeyAction = action.mediaKeyAction,
            showInCompactView = showInCompactView
        )

        public fun <P : CustomActionProvider<M>, M : MediaItem> fromCustomAction(
            customActionClass: Class<P>,
            showInCompactView: Boolean = true
        ): NotificationAction<M> = CustomNotificationAction(
            customActionClass = customActionClass,
            showInCompactView = showInCompactView
        )
    }

}

internal class DefaultNotificationAction<M : MediaItem>(
    @DrawableRes
    private val icon: Int,
    @StringRes
    private val title: Int,
    @MediaKeyAction
    private val mediaKeyAction: Long,
    private val showInCompactView: Boolean
) : NotificationAction<M>() {

    override fun getNotificationActionIcon(
        state: MediaPlayerState<M>,
        customActionProviders: List<CustomActionProvider<M>>
    ) = DefaultNotificationActionIcon(
        icon = icon,
        title = title,
        mediaKeyAction = mediaKeyAction,
        showInCompactView = showInCompactView
    )

}

internal class CustomNotificationAction<P : CustomActionProvider<M>, M : MediaItem>(
    private val customActionClass: Class<P>,
    private val showInCompactView: Boolean
) : NotificationAction<M>() {

    override fun getNotificationActionIcon(
        state: MediaPlayerState<M>,
        customActionProviders: List<CustomActionProvider<M>>
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
