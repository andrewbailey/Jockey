package dev.andrewbailey.encore.player

import android.os.Handler
import android.os.Looper
import android.support.v4.media.MediaBrowserCompat
import androidx.core.os.bundleOf
import androidx.media.MediaBrowserServiceCompat
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertWithMessage
import dev.andrewbailey.encore.player.assertions.mediasession.MediaBrowserItemCorrespondence
import dev.andrewbailey.encore.player.browse.MediaResumptionProvider
import dev.andrewbailey.encore.player.browse.impl.MediaBrowserImpl
import dev.andrewbailey.encore.player.util.EncoreTestRule
import dev.andrewbailey.encore.player.util.mediaBrowserFor
import dev.andrewbailey.encore.test.FakeMusicProvider
import dev.andrewbailey.encore.test.FakeSong
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.android.asCoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withContext
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class MediaResumptionTest {

    private val mediaProvider = FakeMusicProvider()
    private val scheduler = TestCoroutineScheduler()
    private val dispatcher = StandardTestDispatcher(scheduler)

    @get:Rule
    val encoreTestRule = EncoreTestRule()

    @MockK
    lateinit var mediaResumptionProvider: MediaResumptionProvider<FakeSong>

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(dispatcher)
        EncoreTestService.Dependencies.mediaResumptionProviderOverride = mediaResumptionProvider
    }

    @After
    fun tearDown() {
        clearAllMocks()
        Dispatchers.resetMain()
        EncoreTestService.Dependencies.reset()
    }

    @Test
    fun testMediaSessionAccessesResumptionBrowserRoot() = runTest(dispatcher) {
        val lastPlayed = mediaProvider.getAllSongs().first()

        coEvery { mediaResumptionProvider.getCurrentTrack() } coAnswers {
            withContext(dispatcher) {
                delay(5000)
                lastPlayed
            }
        }

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
