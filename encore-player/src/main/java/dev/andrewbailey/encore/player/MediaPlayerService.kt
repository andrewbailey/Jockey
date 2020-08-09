package dev.andrewbailey.encore.player

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.support.v4.media.MediaBrowserCompat
import androidx.media.MediaBrowserServiceCompat
import androidx.media.session.MediaButtonReceiver
import dev.andrewbailey.encore.player.action.CustomActionIntents
import dev.andrewbailey.encore.player.action.CustomActionProvider
import dev.andrewbailey.encore.player.action.QuitActionProvider
import dev.andrewbailey.encore.player.binder.ServiceHostHandler
import dev.andrewbailey.encore.player.browse.BrowserHierarchy
import dev.andrewbailey.encore.player.browse.impl.MediaBrowserImpl
import dev.andrewbailey.encore.player.browse.verification.BrowserClient
import dev.andrewbailey.encore.player.browse.verification.BrowserPackageValidator
import dev.andrewbailey.encore.player.notification.NotificationProvider
import dev.andrewbailey.encore.player.notification.PlaybackNotifier
import dev.andrewbailey.encore.player.os.MediaSessionController
import dev.andrewbailey.encore.player.playback.MediaPlayer
import dev.andrewbailey.encore.player.playback.PlaybackExtension
import dev.andrewbailey.encore.player.playback.PlaybackObserver
import dev.andrewbailey.encore.player.state.MediaPlayerState
import dev.andrewbailey.encore.player.state.PlaybackState
import dev.andrewbailey.encore.player.state.factory.DefaultPlaybackStateFactory
import dev.andrewbailey.encore.player.state.factory.PlaybackStateFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

abstract class MediaPlayerService(
    private val tag: String,
    private val notificationId: Int,
    private val notificationProvider: NotificationProvider,
    private val playbackStateFactory: PlaybackStateFactory = DefaultPlaybackStateFactory(),
    private val extensions: List<PlaybackExtension> = emptyList(),
    private val observers: List<PlaybackObserver> = emptyList()
) : MediaBrowserServiceCompat() {

    private var isBound = false
    private val coroutineScope = CoroutineScope(Dispatchers.Unconfined)

    private lateinit var customActions: List<CustomActionProvider>
    private lateinit var mediaSessionController: MediaSessionController
    private lateinit var notifier: PlaybackNotifier
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var browserPackageValidator: BrowserPackageValidator
    private lateinit var mediaBrowserDelegate: MediaBrowserImpl
    private lateinit var binder: ServiceHostHandler

    override fun onCreate() {
        super.onCreate()
        val browserHierarchy = onCreateMediaBrowserHierarchy()

        customActions = onCreateCustomActions() + listOf(QuitActionProvider(this))
        mediaSessionController = MediaSessionController(this, tag, browserHierarchy)
        binder = ServiceHostHandler(
            getState = { mediaPlayer.getState() },
            getMediaSession = { mediaSessionController.mediaSession },
            onSetState = { mediaPlayer.setState(it) }
        )

        notifier = PlaybackNotifier(
            service = this,
            mediaSession = mediaSessionController.mediaSession,
            notificationId = notificationId,
            notificationProvider = notificationProvider,
            customActionProviders = customActions
        )

        mediaPlayer = MediaPlayer(
            context = applicationContext,
            playbackStateFactory = playbackStateFactory,
            extensions = listOf(
                mediaSessionController,
                *extensions.toTypedArray()
            ),
            observers = listOf(
                notifier,
                binder,
                *observers.toTypedArray()
            )
        )

        browserPackageValidator = BrowserPackageValidator(applicationContext)
        mediaBrowserDelegate = MediaBrowserImpl(coroutineScope, browserHierarchy)

        sessionToken = mediaSessionController.mediaSession.sessionToken
    }

    final override fun onBind(intent: Intent?): IBinder? {
        return if (intent?.action == "android.media.browse.MediaBrowserService") {
            super.onBind(intent)
        } else {
            isBound = true
            binder.messenger.binder
        }
    }

    final override fun onUnbind(intent: Intent?): Boolean {
        return if (intent?.action == "android.media.browse.MediaBrowserService") {
            super.onUnbind(intent)
        } else {
            isBound = false
            return false
        }
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

    fun quit() {
        /*
            If a client is still using the playback service, pause playback and hide the
            notification. There's a chance that the user will resume playback later on, so we
            don't want to kill the service just yet. The OS will eventually kill the service
            if the remaining components unbind from it and the service is left idle.

            If nothing is binding to this service, we can kill it immediately.
         */
        if (isBound) {
            (mediaPlayer.getState() as? MediaPlayerState.Prepared)
                ?.transportState
                ?.takeIf { it.status == PlaybackState.PLAYING }
                ?.let { playbackStateFactory.pause(it) }
                ?.let { mediaPlayer.setState(it) }
            stopForeground(true)
        } else {
            stopSelf()
        }
    }

    protected open fun isClientAllowedToBrowse(
        clientPackageName: String,
        clientUid: Int
    ): Boolean {
        return browserPackageValidator.isClientAllowedToBind(BrowserClient(
            packageName = clientPackageName,
            uid = clientUid
        ))
    }

    final override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot? {
        return if (isClientAllowedToBrowse(clientPackageName, clientUid)) {
            mediaBrowserDelegate.onGetRoot(clientPackageName, clientUid, rootHints)
        } else {
            null
        }
    }

    final override fun onLoadChildren(
        parentId: String,
        result: Result<List<MediaBrowserCompat.MediaItem>>
    ) {
        mediaBrowserDelegate.onLoadChildren(parentId, result)
    }

    open fun onCreateCustomActions(): List<CustomActionProvider> {
        return emptyList()
    }

    abstract fun onCreateMediaBrowserHierarchy(): BrowserHierarchy
}
