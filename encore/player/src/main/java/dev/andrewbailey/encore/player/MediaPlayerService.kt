package dev.andrewbailey.encore.player

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.support.v4.media.MediaBrowserCompat
import androidx.annotation.VisibleForTesting
import androidx.annotation.VisibleForTesting.PRIVATE
import androidx.media.MediaBrowserServiceCompat
import androidx.media.session.MediaButtonReceiver
import dev.andrewbailey.encore.model.MediaObject
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
import dev.andrewbailey.encore.player.state.PlaybackStatus
import dev.andrewbailey.encore.player.state.factory.DefaultPlaybackStateFactory
import dev.andrewbailey.encore.player.state.factory.PlaybackStateFactory
import dev.andrewbailey.encore.provider.MediaProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

public abstract class MediaPlayerService<M : MediaObject> constructor(
    private val tag: String,
    private val notificationId: Int,
    private val notificationProvider: NotificationProvider<M>
) : MediaBrowserServiceCompat() {

    private val coroutineScope = CoroutineScope(Dispatchers.Unconfined)

    private lateinit var playbackStateFactory: PlaybackStateFactory<M>
    private lateinit var extensions: List<PlaybackExtension<M>>
    private lateinit var observers: List<PlaybackObserver<M>>

    @VisibleForTesting(otherwise = PRIVATE)
    internal lateinit var mediaSessionController: MediaSessionController<M>
        private set

    private lateinit var customActions: List<CustomActionProvider<M>>
    private lateinit var notifier: PlaybackNotifier<M>
    private lateinit var mediaPlayer: MediaPlayer<M>
    private lateinit var browserPackageValidator: BrowserPackageValidator
    private lateinit var mediaBrowserDelegate: MediaBrowserImpl<M>
    private lateinit var binder: ServiceHostHandler<M>

    override fun onCreate() {
        super.onCreate()
        playbackStateFactory = onCreatePlaybackStateFactory()
        extensions = onCreatePlaybackExtensions()
        observers = onCreatePlaybackObservers()
        customActions = onCreateCustomActions() + listOf(QuitActionProvider(this))
        val mediaProvider = onCreateMediaProvider()
        val browserHierarchy = onCreateMediaBrowserHierarchy()

        mediaSessionController = MediaSessionController(
            context = this,
            tag = tag,
            playbackStateFactory = playbackStateFactory,
            browserHierarchy = browserHierarchy,
            mediaProvider = mediaProvider
        )

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
            binder.messenger.binder
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
        val playerState = mediaPlayer.getState() as? MediaPlayerState.Prepared
        if (playerState?.transportState?.status != PlaybackStatus.Playing) {
            quit()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel("MediaPlayerService has been destroyed")
        mediaPlayer.release()
    }

    internal fun quit() {
        /*
            If a client is still using the playback service, pause playback and hide the
            notification. There's a chance that the user will resume playback later on, so we
            don't want to kill the service just yet. The OS will eventually kill the service
            if the remaining components unbind from it and the service is left idle.

            If nothing is binding to this service, we can kill it immediately.
         */
        if (binder.hasClients()) {
            (mediaPlayer.getState() as? MediaPlayerState.Prepared)
                ?.transportState
                ?.takeIf { it.status == PlaybackStatus.Playing }
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
        return browserPackageValidator.isClientAllowedToBind(
            BrowserClient(
                packageName = clientPackageName,
                uid = clientUid
            )
        )
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

    public open fun onCreatePlaybackStateFactory(): PlaybackStateFactory<M> {
        return DefaultPlaybackStateFactory()
    }

    public open fun onCreatePlaybackExtensions(): List<PlaybackExtension<M>> {
        return emptyList()
    }

    public open fun onCreatePlaybackObservers(): List<PlaybackObserver<M>> {
        return emptyList()
    }

    public open fun onCreateCustomActions(): List<CustomActionProvider<M>> {
        return emptyList()
    }

    public abstract fun onCreateMediaProvider(): MediaProvider<M>

    public abstract fun onCreateMediaBrowserHierarchy(): BrowserHierarchy<M>
}
