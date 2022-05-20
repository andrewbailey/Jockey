package dev.andrewbailey.encore.player

import android.os.Handler
import android.os.Looper
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaControllerCompat
import androidx.core.os.bundleOf
import androidx.media.MediaBrowserServiceCompat
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertWithMessage
import dev.andrewbailey.encore.model.QueueItem
import dev.andrewbailey.encore.player.assertions.mediasession.MediaBrowserItemCorrespondence
import dev.andrewbailey.encore.player.assertions.mediasession.mediaMetadataCompat
import dev.andrewbailey.encore.player.assertions.mediasession.model.MediaMetadataKey
import dev.andrewbailey.encore.player.assertions.mediasession.model.PlaybackStateConstants
import dev.andrewbailey.encore.player.assertions.mediasession.playbackStateCompat
import dev.andrewbailey.encore.player.browse.MediaResumptionProvider
import dev.andrewbailey.encore.player.browse.impl.MediaBrowserImpl
import dev.andrewbailey.encore.player.state.PlaybackStatus
import dev.andrewbailey.encore.player.state.QueueState
import dev.andrewbailey.encore.player.state.RepeatMode
import dev.andrewbailey.encore.player.state.SeekPosition
import dev.andrewbailey.encore.player.state.TransportState
import dev.andrewbailey.encore.player.util.EncoreTestRule
import dev.andrewbailey.encore.player.util.mediaBrowserFor
import dev.andrewbailey.encore.player.util.mediaControllerFrom
import dev.andrewbailey.encore.test.FakeMusicProvider
import dev.andrewbailey.encore.test.FakeSong
import java.util.UUID
import kotlinx.coroutines.android.asCoroutineDispatcher
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.CONFLATED
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.After
import org.junit.Rule
import org.junit.Test

class MediaResumptionTest {

    private val mediaProvider = FakeMusicProvider()
    private val scheduler = TestCoroutineScheduler()
    private val dispatcher = StandardTestDispatcher(scheduler)

    @get:Rule
    val encoreTestRule = EncoreTestRule()

    private var mediaResumptionProvider: MediaResumptionProvider<FakeSong>
        get() = EncoreTestService.Dependencies.mediaResumptionProviderOverride
            ?: throw UninitializedPropertyAccessException("mediaResumptionProvider was not set")
        set(value) {
            EncoreTestService.Dependencies.mediaResumptionProviderOverride = value
        }

    @After
    fun tearDown() {
        EncoreTestService.Dependencies.reset()
        scheduler.cancel()
    }

    @Test
    fun testMediaSessionAccessesResumptionBrowserRoot() = runTest(dispatcher) {
        val lastPlayed = mediaProvider.getAllSongs().first()

        mediaResumptionProvider = createMediaResumptionProvider(
            getCurrentTrack = {
                withContext(dispatcher) {
                    delay(5000)
                    lastPlayed
                }
            },
            getPersistedTransportState = { null }
        )

        val mediaBrowser = getMediaBrowser(recentRootHint = true)

        try {
            val root = mediaBrowser.root

            assertWithMessage("The returned root path did not match the expected value")
                .that(root)
                .isEqualTo(MediaBrowserImpl.MEDIA_RESUMPTION_ROOT)

            var items: List<MediaBrowserCompat.MediaItem>? = null
            var hasError = false
            val callback = mediaBrowserSubscriptionCallback(
                onChildrenLoaded = { items = it },
                onError = { hasError = true }
            )

            mediaBrowser.subscribe(root, callback)
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()
            scheduler.advanceUntilIdle()
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()

            assertWithMessage("The media browser raised an error after querying the recents root")
                .that(hasError)
                .isFalse()

            assertWithMessage("The recents root did return any data")
                .that(items)
                .isNotNull()

            assertWithMessage("The recents root did not contain the expected items")
                .that(items)
                .comparingElementsUsing(MediaBrowserItemCorrespondence)
                .containsExactly(lastPlayed)
        } finally {
            mediaBrowser.disconnect()
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        }
    }

    /**
     * This test sets out to mimic what SystemUI will do when resuming playback via MediaResumption.
     * The sequence of operations comes from this blog post (See Figure 7 in particular):
     * https://android-developers.googleblog.com/2020/08/playing-nicely-with-media-controls.html
     */
    @Test
    fun testPlayStartsServiceAndResumesPlayback() = runTest(dispatcher) {
        val savedState = TransportState.Populated(
            status = PlaybackStatus.Paused(false),
            seekPosition = SeekPosition.AbsoluteSeekPosition(15_000),
            queue = QueueState.Linear(
                queueIndex = 3,
                queue = mediaProvider.getAllSongs().map {
                    QueueItem(
                        queueId = UUID.randomUUID(),
                        mediaItem = it
                    )
                }
            ),
            repeatMode = RepeatMode.RepeatNone,
            playbackSpeed = 1f
        )

        val getPersistedStateContinuationSignal = Channel<Unit>(CONFLATED)
        mediaResumptionProvider = createMediaResumptionProvider(
            getCurrentTrack = {
                savedState.queue.nowPlaying.mediaItem
            },
            getPersistedTransportState = {
                getPersistedStateContinuationSignal.receive()
                savedState
            }
        )

        val mediaBrowser = getMediaBrowser(true)
        try {
            // Connect to MediaSession
            val mediaSessionController = mediaControllerFrom(mediaBrowser)

            // Assert idle
            testScheduler.runCurrent()
            assertMediaSessionIdle(
                message = "The MediaSession had an unexpected initial state " +
                    "(was expected to be fully idle)",
                mediaSessionController = mediaSessionController
            )

            // Press Play
            mediaSessionController.transportControls.play()

            // Assert still idle
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()
            testScheduler.runCurrent()
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()
            assertMediaSessionIdle(
                message = "The MediaSession left the idle state unexpectedly. The player should " +
                    "remain idle until the previous state is fully restored.",
                mediaSessionController = mediaSessionController
            )

            // Trigger the persistence lookup to finish
            getPersistedStateContinuationSignal.send(Unit)
            testScheduler.advanceUntilIdle()
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()

            // Assert playing in correct state
            assertWithMessage(
                "The MediaSession did not have the expected metadata after restoring the " +
                    "persisted state."
            )
                .about(mediaMetadataCompat())
                .that(mediaSessionController.metadata)
                .hasMetadata(
                    MediaMetadataKey.Title to "Molecular Opulence",
                    MediaMetadataKey.Album to "Illogical Capacitance",
                    MediaMetadataKey.Artist to "Diode Discontinuity"
                )

            assertWithMessage(
                "The MediaSession was not playing after restoring the persisted state."
            )
                .about(playbackStateCompat())
                .that(mediaSessionController.playbackState)
                .state()
                .isEqualTo(PlaybackStateConstants.State.Playing)

            assertWithMessage(
                "The MediaSession did not restore the expected seek position."
            )
                .that(mediaSessionController.playbackState.position)
                .isGreaterThan(15_000)
        } finally {
            mediaBrowser.disconnect()
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        }

    }

    private fun createMediaResumptionProvider(
        persistState: suspend (transportState: TransportState<FakeSong>) -> Boolean = {
            true
        },
        getCurrentTrack: suspend () -> FakeSong? = {
            throw UnsupportedOperationException("getCurrentTrack() not mocked")
        },
        getPersistedTransportState: suspend () -> TransportState<FakeSong>? = {
            throw UnsupportedOperationException("getPersistedTransportState() not mocked")
        }
    ) = object : MediaResumptionProvider<FakeSong>(dispatcher) {
        override suspend fun persistState(transportState: TransportState<FakeSong>): Boolean {
            return persistState(transportState)
        }

        override suspend fun getCurrentTrack(): FakeSong? {
            return getCurrentTrack()
        }

        override suspend fun getPersistedTransportState(): TransportState<FakeSong>? {
            return getPersistedTransportState()
        }

    }

    private fun assertMediaSessionIdle(
        message: String,
        mediaSessionController: MediaControllerCompat
    ) {
        assertWithMessage(message)
            .about(playbackStateCompat())
            .that(mediaSessionController.playbackState)
            .state()
            .isEqualTo(PlaybackStateConstants.State.None)

        assertWithMessage(message)
            .about(mediaMetadataCompat())
            .that(mediaSessionController.metadata)
            .hasNoMetadataValues()
    }

    private suspend fun getMediaBrowser(
        recentRootHint: Boolean
    ): MediaBrowserCompat {
        return withContext(Handler(Looper.getMainLooper()).asCoroutineDispatcher()) {
            mediaBrowserFor<EncoreTestService>(
                rootHints = bundleOf(
                    MediaBrowserServiceCompat.BrowserRoot.EXTRA_RECENT to recentRootHint
                )
            )
        }
    }

    private inline fun mediaBrowserSubscriptionCallback(
        crossinline onChildrenLoaded: (children: List<MediaBrowserCompat.MediaItem>) -> Unit = {},
        crossinline onError: () -> Unit = {}
    ) = object : MediaBrowserCompat.SubscriptionCallback() {
        override fun onChildrenLoaded(
            parentId: String,
            children: MutableList<MediaBrowserCompat.MediaItem>
        ) {
            onChildrenLoaded(children)
        }

        override fun onError(parentId: String) {
            onError()
        }
    }

}
