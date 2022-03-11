package dev.andrewbailey.encore.player

import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertWithMessage
import dev.andrewbailey.encore.model.QueueItem
import dev.andrewbailey.encore.player.assertions.encore.mediaPlayerState
import dev.andrewbailey.encore.player.controller.EncoreController
import dev.andrewbailey.encore.player.state.BufferingState
import dev.andrewbailey.encore.player.state.MediaPlayerState
import dev.andrewbailey.encore.player.state.PlaybackState
import dev.andrewbailey.encore.player.state.QueueState
import dev.andrewbailey.encore.player.state.RepeatMode
import dev.andrewbailey.encore.player.state.SeekPosition
import dev.andrewbailey.encore.player.state.ShuffleMode
import dev.andrewbailey.encore.player.state.TransportState
import dev.andrewbailey.encore.player.util.EncoreTestRule
import dev.andrewbailey.encore.test.FakeMusicProvider
import dev.andrewbailey.encore.test.FakeSong
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.Timeout

class EncoreIntegrationTest {

    @get:Rule
    val encoreTestRule = EncoreTestRule()

    @get:Rule
    val timeoutRule = Timeout(5, TimeUnit.SECONDS)

    private lateinit var mediaProvider: FakeMusicProvider

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().context
        mediaProvider = FakeMusicProvider(context)
    }

    @Test
    fun testEncoreInitializesToIdleState() = encoreTest { encoreController ->
        assertWithMessage("The Encore service did not initialize to the expected state")
            .that(encoreController.getState())
            .isEqualTo(
                MediaPlayerState.Ready(
                    transportState = TransportState.Idle(
                        repeatMode = RepeatMode.REPEAT_NONE,
                        shuffleMode = ShuffleMode.LINEAR
                    )
                )
            )
    }

    @Test
    fun testSetStateFromIdleToActiveAndPlaying() = encoreTest { encoreController ->
        val transportState = TransportState.Active(
            status = PlaybackState.PLAYING,
            seekPosition = SeekPosition.AbsoluteSeekPosition(0),
            repeatMode = RepeatMode.REPEAT_NONE,
            queue = QueueState.Linear(
                queueIndex = 0,
                queue = mediaProvider.getAllSongs().map {
                    QueueItem(
                        queueId = UUID.randomUUID(),
                        mediaItem = it
                    )
                }
            )
        )

        encoreController.setState(transportState)

        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        encoreController.waitForPlayerToBuffer()

        val actualState = encoreController.getState()

        assertWithMessage("The player did not enter the expected state")
            .about(mediaPlayerState())
            .that(actualState)
            .isInstanceOf<MediaPlayerState.Prepared<FakeSong>>()

        assertWithMessage("The player was not playing media")
            .about(mediaPlayerState())
            .that(actualState)
            .transportState()
            .hasStatus(PlaybackState.PLAYING)

        assertWithMessage("The player is not at the beginning of the track")
            .about(mediaPlayerState())
            .that(actualState)
            .transportState()
            .hasSeekPosition(0)

        assertWithMessage("The player did not have the expected repeat mode")
            .about(mediaPlayerState())
            .that(actualState)
            .transportState()
            .repeatMode()
            .isEqualTo(RepeatMode.REPEAT_NONE)

        assertWithMessage("The player did not have the expected shuffle mode")
            .about(mediaPlayerState())
            .that(actualState)
            .transportState()
            .shuffleMode()
            .isEqualTo(ShuffleMode.LINEAR)

        assertWithMessage("The player did not have the expected queue state")
            .about(mediaPlayerState())
            .that(actualState)
            .transportState()
            .hasQueueState(transportState.queue)

        assertWithMessage("The player was not expected to have album artwork")
            .about(mediaPlayerState())
            .that(actualState)
            .hasArtwork(null)

        assertWithMessage("The player did not set the expected duration")
            .about(mediaPlayerState())
            .that(actualState)
            .hasDuration(30_000)
    }
    private inline fun encoreTest(
        crossinline action: suspend (encoreController: EncoreController<FakeSong>) -> Unit
    ) {
        encoreTestRule.withEncore { encoreController ->
            runTest {
                action(encoreController)
            }
        }
    }

    private suspend fun EncoreController<*>.waitForPlayerToBuffer() {
        while (
            (getState() as? MediaPlayerState.Prepared<*>)?.bufferingState
            is BufferingState.Buffering
        ) {
            delay(100)
        }
    }

}
