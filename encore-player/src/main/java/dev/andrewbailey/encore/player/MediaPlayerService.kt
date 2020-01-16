package dev.andrewbailey.encore.player

import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.media.session.MediaButtonReceiver
import dev.andrewbailey.encore.player.action.CustomActionIntents
import dev.andrewbailey.encore.player.action.CustomActionProvider
import dev.andrewbailey.encore.player.notification.NotificationProvider
import dev.andrewbailey.encore.player.notification.PlaybackNotifier
import dev.andrewbailey.encore.player.os.MediaSessionController
import dev.andrewbailey.encore.player.playback.MediaPlayer
import dev.andrewbailey.encore.player.playback.PlaybackExtension
import dev.andrewbailey.encore.player.playback.PlaybackObserver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

abstract class MediaPlayerService(
    tag: String,
    notificationId: Int,
    notificationProvider: NotificationProvider,
    extensions: List<PlaybackExtension> = emptyList(),
    observers: List<PlaybackObserver> = emptyList()
) : Service() {

    private val coroutineScope = CoroutineScope(Dispatchers.Unconfined)

    private val customActions by lazy {
        onCreateCustomActions()
    }

    private val mediaSessionController by lazy {
        MediaSessionController(this, tag)
    }

    private val notifier by lazy {
        PlaybackNotifier(
            service = this,
            mediaSession = mediaSessionController.mediaSession,
            notificationId = notificationId,
            notificationProvider = notificationProvider,
            customActionProviders = customActions
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
        intent?.let { handleCommand(it) }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun handleCommand(intent: Intent) {
        if (intent.hasExtra(Intent.EXTRA_KEY_EVENT)) {
            if (Build.VERSION.SDK_INT >= 26) {
                /*
                 * We need to make sure we call startForeground since MediaButtonReceiver always
                 * calls startForegroundService. Otherwise our service might get killed if the
                 * service is in the background when it receives the command and does not end up
                 * in a state that does not normally require a foreground notification.
                 */
                notifier.showNotification(foreground = true, playbackState = mediaPlayer.getState())
            }
            MediaButtonReceiver.handleIntent(mediaSessionController.mediaSession, intent)
        } else if (CustomActionIntents.isCustomActionIntent(intent)) {
            val actionId = CustomActionIntents.parseActionIdFromIntent(intent)
            val actionProvider = customActions.firstOrNull { it.id == actionId } ?: return
            coroutineScope.launch {
                actionProvider.performAction(mediaPlayer.getState())
            }
        }
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel("MediaPlayerService has been destroyed")
        mediaPlayer.release()
    }

    open fun onCreateCustomActions(): List<CustomActionProvider> {
        return emptyList()
    }
}
