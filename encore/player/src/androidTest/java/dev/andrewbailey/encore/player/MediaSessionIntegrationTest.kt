package dev.andrewbailey.encore.player

import android.content.Intent
import android.net.Uri
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertWithMessage
import dev.andrewbailey.encore.player.assertions.mediasession.mediaControllerCompat
import dev.andrewbailey.encore.player.assertions.mediasession.model.MediaDescriptionKey
import dev.andrewbailey.encore.player.assertions.mediasession.model.MediaMetadataKey
import dev.andrewbailey.encore.player.assertions.mediasession.model.PlaybackStateConstants.Action.Pause
import dev.andrewbailey.encore.player.assertions.mediasession.model.PlaybackStateConstants.Action.Play
import dev.andrewbailey.encore.player.assertions.mediasession.model.PlaybackStateConstants.Action.PlayFromMediaId
import dev.andrewbailey.encore.player.assertions.mediasession.model.PlaybackStateConstants.Action.PlayFromSearch
import dev.andrewbailey.encore.player.assertions.mediasession.model.PlaybackStateConstants.Action.PlayPause
import dev.andrewbailey.encore.player.assertions.mediasession.model.PlaybackStateConstants.Action.SeekTo
import dev.andrewbailey.encore.player.assertions.mediasession.model.PlaybackStateConstants.Action.SetRepeatMode
import dev.andrewbailey.encore.player.assertions.mediasession.model.PlaybackStateConstants.Action.SetShuffleMode
import dev.andrewbailey.encore.player.assertions.mediasession.model.PlaybackStateConstants.Action.SkipToNext
import dev.andrewbailey.encore.player.assertions.mediasession.model.PlaybackStateConstants.Action.SkipToPrevious
import dev.andrewbailey.encore.player.assertions.mediasession.model.PlaybackStateConstants.Action.Stop
import dev.andrewbailey.encore.player.assertions.mediasession.model.PlaybackStateConstants.State
import dev.andrewbailey.encore.player.util.mediaBrowserFor
import dev.andrewbailey.encore.player.util.mediaControllerFrom
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.Timeout

class MediaSessionIntegrationTest {

    @get:Rule
    val timeoutRule = Timeout(5, TimeUnit.SECONDS)

    private lateinit var mediaBrowser: MediaBrowserCompat
    private lateinit var mediaController: MediaControllerCompat

    @Before
    fun setUp() {
        println("Setting up...")
        runBlocking(Dispatchers.Main) {
            println("runningBlocking stuff")
            mediaBrowser = mediaBrowserFor<EncoreTestService>()
            mediaController = mediaControllerFrom(mediaBrowser)
            println("Finished initialization")
        }

        assertWithMessage(
            "The MediaSession did not initialize in a clean state. " +
                "Was it not reset between tests?"
        )
            .about(mediaControllerCompat())
            .that(mediaController)
            .playbackState()
            .hasState(State.None)
    }

    @After
    fun tearDown() {
        mediaBrowser.disconnect()
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        context.stopService(Intent(context, EncoreTestService::class.java))
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
    }

    // region Test cases

    @Test
    fun testColdStartMediaSessionInitialization() {
        assertMediaSessionIdle()
    }

    @Test
    fun testPlayFromMediaBrowserIdAction() {
        val browserId = "/albums@[album-001]/contents$[song-001]"
        mediaController.transportControls.playFromMediaId(browserId, null)

        waitForState("Timed out while waiting for the MediaSession state to change.") {
            playbackState.state != PlaybackStateCompat.STATE_NONE
        }

        assertMediaSessionPlaying(
            expectedState = State.Playing,
            expectedMetadata = mapOf(
                MediaMetadataKey.Title to "Equator",
                MediaMetadataKey.Artist to "All Logic",
                MediaMetadataKey.Album to "Oracle"
            ),
            expectedQueue = listOf(
                0L to mediaDescriptionFor("song-001", "Equator"),
                1L to mediaDescriptionFor("song-002", "Octet"),
                2L to mediaDescriptionFor("song-003", "Roundabout")
            )
        )
    }

    // endregion Test cases

    // region Test utility functions

    private fun waitForState(
        message: String,
        timeoutMs: Long = 5000,
        predicate: MediaControllerCompat.() -> Boolean
    ) = runBlocking {
        try {
            withTimeout(timeoutMs) {
                while (!mediaController.predicate()) {
                    delay(100)
                }
            }
        } catch (_: TimeoutCancellationException) {
            assertWithMessage(message).fail()
        }
    }

    private fun mediaDescriptionFor(
        mediaId: String,
        title: String,
    ): Map<MediaDescriptionKey<Any?>, Any?> {
        return mapOf(
            MediaDescriptionKey.MediaId to mediaId,
            MediaDescriptionKey.Title to title,
            MediaDescriptionKey.MediaUri to Uri.parse(
                "android.resource://dev.andrewbailey.encore.player.test/raw/silence"
            )
        )
    }

    private fun assertMediaSessionIdle() {
        assertWithMessage("MediaSession should be ready")
            .about(mediaControllerCompat())
            .that(mediaController)
            .sessionIsReady()

        assertWithMessage("MediaSession did not have correct playback state")
            .about(mediaControllerCompat())
            .that(mediaController)
            .playbackState()
            .hasState(State.None)

        assertWithMessage("MediaSession did not report the expected actions")
            .about(mediaControllerCompat())
            .that(mediaController)
            .playbackState()
            .hasActions(SetRepeatMode, SetShuffleMode, PlayFromMediaId, PlayFromSearch)

        assertWithMessage("MediaSession did not have the expected metadata")
            .about(mediaControllerCompat())
            .that(mediaController)
            .metadata()
            .hasNoMetadataValues()

        assertWithMessage("MediaSession did not report an empty queue")
            .about(mediaControllerCompat())
            .that(mediaController)
            .queue()
            .isEmpty()
    }

    private fun assertMediaSessionPlaying(
        expectedState: State,
        expectedMetadata: Map<MediaMetadataKey<Any>, Any?>,
        expectedQueue: List<Pair<Long, Map<MediaDescriptionKey<*>, *>>>
    ) {
        assertWithMessage("MediaSession should be ready")
            .about(mediaControllerCompat())
            .that(mediaController)
            .sessionIsReady()

        assertWithMessage("MediaSession did not have correct playback state")
            .about(mediaControllerCompat())
            .that(mediaController)
            .playbackState()
            .hasState(expectedState)

        assertWithMessage("MediaSession did not have the expected metadata")
            .about(mediaControllerCompat())
            .that(mediaController)
            .metadata()
            .hasMetadata(expectedMetadata)

        assertWithMessage("MediaSession did not report the expected queue")
            .about(mediaControllerCompat())
            .that(mediaController)
            .queueExactlyMatches(expectedQueue)

        assertWithMessage("MediaSession did not report the expected actions")
            .about(mediaControllerCompat())
            .that(mediaController)
            .playbackState()
            .hasActions(
                Stop, Pause, Play, SkipToPrevious, SkipToNext, SeekTo, PlayPause,
                PlayFromMediaId, PlayFromSearch, SetRepeatMode, SetShuffleMode
            )
    }

    // endregion Test utility functions

}
