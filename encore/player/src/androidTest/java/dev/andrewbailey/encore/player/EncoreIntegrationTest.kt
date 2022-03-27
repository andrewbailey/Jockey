package dev.andrewbailey.encore.player

import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertWithMessage
import dev.andrewbailey.encore.model.QueueItem
import dev.andrewbailey.encore.player.assertions.encore.mediaPlayerState
import dev.andrewbailey.encore.player.controller.EncoreController
import dev.andrewbailey.encore.player.state.BufferingState
import dev.andrewbailey.encore.player.state.MediaPlayerState
import dev.andrewbailey.encore.player.state.PlaybackStatus
import dev.andrewbailey.encore.player.state.QueueState
import dev.andrewbailey.encore.player.state.RepeatMode
import dev.andrewbailey.encore.player.state.SeekPosition
import dev.andrewbailey.encore.player.state.ShuffleMode
import dev.andrewbailey.encore.player.state.TransportState
import dev.andrewbailey.encore.player.state.copy
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

    // region Test cases

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
            status = PlaybackStatus.Playing,
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

        encoreController.setStateAndWaitForIdle(transportState)

        val actualState = encoreController.getState()

        assertWithMessage("The player did not enter the expected state")
            .about(mediaPlayerState())
            .that(actualState)
            .isInstanceOf<MediaPlayerState.Prepared<FakeSong>>()

        assertWithMessage("The player was not playing media")
            .about(mediaPlayerState())
            .that(actualState)
            .transportState()
            .hasStatus(PlaybackStatus.Playing)

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

    @Test
    fun testSetStateFromIdleToActiveAndPaused() = encoreTest { encoreController ->
        val transportState = createActiveState(
            status = PlaybackStatus.Paused(),
            seekPositionMs = 5000
        )

        encoreController.setStateAndWaitForIdle(transportState)

        delay(100)

        val actualState = encoreController.getState()

        assertWithMessage("The player was not in the paused state")
            .about(mediaPlayerState())
            .that(actualState)
            .transportState()
            .hasStatus(PlaybackStatus.Paused())

        assertWithMessage("The seek position did not match the requested value")
            .about(mediaPlayerState())
            .that(actualState)
            .transportState()
            .hasExactSeekPosition(5_000)
    }

    @Test
    fun testSetStateFromActiveToIdle() = encoreTest { encoreController ->
        val activeState = createActiveState(PlaybackStatus.Playing)

        encoreController.setStateAndWaitForIdle(activeState)

        assertWithMessage("The player was not initialized in the active state")
            .about(mediaPlayerState())
            .that(encoreController.getState())
            .transportState()
            .hasStatus(PlaybackStatus.Playing)

        val idleState = TransportState.Idle(
            repeatMode = RepeatMode.REPEAT_NONE,
            shuffleMode = ShuffleMode.LINEAR
        )

        encoreController.setStateAndWaitForIdle(idleState)

        assertWithMessage("The player did not return to the idle state")
            .about(mediaPlayerState())
            .that(encoreController.getState())
            .transportState()
            .isEqualTo(idleState)
    }

    @Test
    fun testPlayWhileIdle() = encoreTest { encoreController ->
        encoreController.checkIdle()

        encoreController.play()
        encoreController.waitForStateToSettle()

        assertWithMessage("The player should remain idle after calling play() in the idle state")
            .about(mediaPlayerState())
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
    fun testPlayWhilePaused() = encoreTest { encoreController ->
        val originalState = createActiveState(PlaybackStatus.Paused())
        val desiredState = originalState.copy(status = PlaybackStatus.Playing)

        encoreController.setStateAndWaitForIdle(originalState)
        encoreController.checkPlaybackStatus(PlaybackStatus.Paused())

        encoreController.play()
        encoreController.waitForStateToSettle()

        assertWithMessage("The player should be playing after calling play() in the paused state")
            .about(mediaPlayerState())
            .that(encoreController.getState())
            .transportState()
            .isEqualTo(desiredState, seekToleranceMs = 500)
    }

    @Test
    fun testPlayWhilePlaying() = encoreTest { encoreController ->
        val originalState = createActiveState(PlaybackStatus.Playing)
        encoreController.setStateAndWaitForIdle(originalState)
        encoreController.checkPlaybackStatus(PlaybackStatus.Playing)

        encoreController.play()
        encoreController.waitForStateToSettle()

        assertWithMessage("The player should do nothing after calling play() when already playing")
            .about(mediaPlayerState())
            .that(encoreController.getState())
            .transportState()
            .isEqualTo(originalState, seekToleranceMs = 500)
    }

    @Test
    fun testPauseWhileIdle() = encoreTest { encoreController ->
        encoreController.checkIdle()

        encoreController.pause()
        encoreController.waitForStateToSettle()

        assertWithMessage("The player should remain idle after calling pause() in the idle state")
            .about(mediaPlayerState())
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
    fun testPauseWhilePaused() = encoreTest { encoreController ->
        val originalState = createActiveState(PlaybackStatus.Paused())
        encoreController.setStateAndWaitForIdle(originalState)
        encoreController.checkPlaybackStatus(PlaybackStatus.Paused())

        encoreController.pause()
        encoreController.waitForStateToSettle()

        assertWithMessage("The player should do nothing after calling pause() when already paused")
            .about(mediaPlayerState())
            .that(encoreController.getState())
            .transportState()
            .isEqualTo(originalState)
    }

    @Test
    fun testPauseWhilePlaying() = encoreTest { encoreController ->
        val originalState = createActiveState(PlaybackStatus.Playing)
        val desiredState = originalState.copy(status = PlaybackStatus.Paused())

        encoreController.setStateAndWaitForIdle(originalState)
        encoreController.checkPlaybackStatus(PlaybackStatus.Playing)

        encoreController.pause()
        encoreController.waitForStateToSettle()

        assertWithMessage("The player should be paused after calling pause() in the playing state")
            .about(mediaPlayerState())
            .that(encoreController.getState())
            .transportState()
            .isEqualTo(desiredState, seekToleranceMs = 500)
    }

    @Test
    fun testSkipPreviousWhileIdle() = encoreTest { encoreController ->
        encoreController.checkIdle()

        encoreController.skipPrevious()
        encoreController.waitForStateToSettle()

        assertWithMessage("The player should remain idle after calling skipPrevious() while idle")
            .about(mediaPlayerState())
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
    fun testSkipPreviousWhilePausedNearMiddleOfTrack() = encoreTest { encoreController ->
        val originalState = createActiveState(
            status = PlaybackStatus.Paused(),
            queueIndex = 3,
            seekPositionMs = 15_000
        )

        val desiredState = originalState.copy(
            status = PlaybackStatus.Playing,
            seekPosition = SeekPosition.AbsoluteSeekPosition(0)
        )

        encoreController.setStateAndWaitForIdle(originalState)
        encoreController.checkPlaybackStatus(PlaybackStatus.Paused())

        encoreController.skipPrevious()
        encoreController.waitForStateToSettle()

        assertWithMessage(
            "The track should restart when calling skipPrevious() when " +
                "playback is near the middle of the track"
        )
            .about(mediaPlayerState())
            .that(encoreController.getState())
            .transportState()
            .isEqualTo(desiredState, seekToleranceMs = 500)
    }

    @Test
    fun testSkipPreviousWhilePausedNearBeginningOfTrack() = encoreTest { encoreController ->
        val originalState = createActiveState(
            status = PlaybackStatus.Paused(),
            queueIndex = 3,
            seekPositionMs = 2_500
        )

        val desiredState = originalState.copy(
            status = PlaybackStatus.Playing,
            seekPosition = SeekPosition.AbsoluteSeekPosition(0),
            queue = originalState.queue.copy(queueIndex = 2)

        )

        encoreController.setStateAndWaitForIdle(originalState)
        encoreController.checkPlaybackStatus(PlaybackStatus.Paused())

        encoreController.skipPrevious()
        encoreController.waitForStateToSettle()

        assertWithMessage(
            "The play should skip to the previous track after calling " +
                "skipPrevious() when playback is near the middle of the track"
        )
            .about(mediaPlayerState())
            .that(encoreController.getState())
            .transportState()
            .isEqualTo(desiredState, seekToleranceMs = 500)
    }

    @Test
    fun testSkipPreviousWhilePausedNearBeginningOfTrackAndQueue() = encoreTest { encoreController ->
        val originalState = createActiveState(
            status = PlaybackStatus.Paused(),
            queueIndex = 0,
            seekPositionMs = 2_500
        )

        val desiredState = originalState.copy(
            status = PlaybackStatus.Playing,
            seekPosition = SeekPosition.AbsoluteSeekPosition(0)
        )

        encoreController.setStateAndWaitForIdle(originalState)
        encoreController.checkPlaybackStatus(PlaybackStatus.Paused())

        encoreController.skipPrevious()
        encoreController.waitForStateToSettle()

        assertWithMessage(
            "The track should restart when calling skipPrevious() when " +
                "playing the first song in the queue"
        )
            .about(mediaPlayerState())
            .that(encoreController.getState())
            .transportState()
            .isEqualTo(desiredState, seekToleranceMs = 500)
    }

    @Test
    fun testSkipPreviousWhilePlayingNearMiddleOfTrack() = encoreTest { encoreController ->
        val originalState = createActiveState(
            status = PlaybackStatus.Playing,
            queueIndex = 3,
            seekPositionMs = 15_000
        )

        val desiredState = originalState.copy(
            status = PlaybackStatus.Playing,
            seekPosition = SeekPosition.AbsoluteSeekPosition(0)
        )

        encoreController.setStateAndWaitForIdle(originalState)
        encoreController.checkPlaybackStatus(PlaybackStatus.Playing)

        encoreController.skipPrevious()
        encoreController.waitForStateToSettle()

        assertWithMessage(
            "The track should restart when calling skipPrevious() when " +
                "playback is near the middle of the track"
        )
            .about(mediaPlayerState())
            .that(encoreController.getState())
            .transportState()
            .isEqualTo(desiredState, seekToleranceMs = 500)
    }

    @Test
    fun testSkipPreviousWhilePlayingNearBeginningOfTrack() = encoreTest { encoreController ->
        val originalState = createActiveState(
            status = PlaybackStatus.Playing,
            queueIndex = 3,
            seekPositionMs = 2_500
        )

        val desiredState = originalState.copy(
            status = PlaybackStatus.Playing,
            seekPosition = SeekPosition.AbsoluteSeekPosition(0),
            queue = originalState.queue.copy(queueIndex = 2)

        )

        encoreController.setStateAndWaitForIdle(originalState)
        encoreController.checkPlaybackStatus(PlaybackStatus.Playing)

        encoreController.skipPrevious()
        encoreController.waitForStateToSettle()

        assertWithMessage(
            "The player should skip to the previous track after calling " +
                "skipPrevious() when playback is near the middle of the track"
        )
            .about(mediaPlayerState())
            .that(encoreController.getState())
            .transportState()
            .isEqualTo(desiredState, seekToleranceMs = 500)
    }

    @Test
    fun testSkipNextWhilePlaying() = encoreTest { encoreController ->
        val originalState = createActiveState(
            status = PlaybackStatus.Playing,
            queueIndex = 3,
            seekPositionMs = 2_500
        )

        val desiredState = originalState.copy(
            status = PlaybackStatus.Playing,
            seekPosition = SeekPosition.AbsoluteSeekPosition(0),
            queue = originalState.queue.copy(queueIndex = 4)

        )

        encoreController.setStateAndWaitForIdle(originalState)
        encoreController.checkPlaybackStatus(PlaybackStatus.Playing)

        encoreController.skipNext()
        encoreController.waitForStateToSettle()

        assertWithMessage(
            "The player should skip to the next track after calling " +
                "skipNext() when the player is playing"
        )
            .about(mediaPlayerState())
            .that(encoreController.getState())
            .transportState()
            .isEqualTo(desiredState, seekToleranceMs = 500)
    }

    @Test
    fun testSkipNextWhilePaused() = encoreTest { encoreController ->
        val originalState = createActiveState(
            status = PlaybackStatus.Paused(),
            queueIndex = 3,
            seekPositionMs = 2_500
        )

        val desiredState = originalState.copy(
            status = PlaybackStatus.Playing,
            seekPosition = SeekPosition.AbsoluteSeekPosition(0),
            queue = originalState.queue.copy(queueIndex = 4)
        )

        encoreController.setStateAndWaitForIdle(originalState)
        encoreController.checkPlaybackStatus(PlaybackStatus.Paused())

        encoreController.skipNext()
        encoreController.waitForStateToSettle()

        assertWithMessage(
            "The player should skip to the next track after calling " +
                "skipNext() when the player is paused"
        )
            .about(mediaPlayerState())
            .that(encoreController.getState())
            .transportState()
            .isEqualTo(desiredState, seekToleranceMs = 500)
    }

    @Test
    fun testSkipNextRestartsQueueWithRepeatEnabled() = encoreTest { encoreController ->
        val originalState = createActiveState(
            status = PlaybackStatus.Playing,
            queueIndex = 2,
            songs = mediaProvider.getAllSongs().take(3),
            repeatMode = RepeatMode.REPEAT_ALL
        )

        val desiredState = originalState.copy(
            status = PlaybackStatus.Playing,
            seekPosition = SeekPosition.AbsoluteSeekPosition(0),
            queue = originalState.queue.copy(queueIndex = 0)
        )

        encoreController.setStateAndWaitForIdle(originalState)
        encoreController.checkPlaybackStatus(PlaybackStatus.Playing)

        encoreController.skipNext()
        encoreController.waitForStateToSettle()

        assertWithMessage(
            "The player should restart the queue after calling skipNext() when the " +
                "player is playing the last song in the queue and repeat all is enabled"
        )
            .about(mediaPlayerState())
            .that(encoreController.getState())
            .transportState()
            .isEqualTo(desiredState, seekToleranceMs = 500)
    }

    @Test
    fun testSkipNextWhilePausedOnLastTrack() = encoreTest { encoreController ->
        val originalState = createActiveState(
            status = PlaybackStatus.Paused(),
            queueIndex = 2,
            songs = mediaProvider.getAllSongs().take(3),
        )

        val desiredState = originalState.copy(
            status = PlaybackStatus.Paused(reachedEndOfQueue = true),
            seekPosition = SeekPosition.AbsoluteSeekPosition(30_167)
        )

        encoreController.setStateAndWaitForIdle(originalState)
        encoreController.checkPlaybackStatus(PlaybackStatus.Paused())

        encoreController.skipNext()
        encoreController.waitForStateToSettle()

        assertWithMessage(
            "The player should skip to the end of the track after calling " +
                "skipNext() when the player is paused at the last song in the queue"
        )
            .about(mediaPlayerState())
            .that(encoreController.getState())
            .transportState()
            .isEqualTo(desiredState, seekToleranceMs = 0)
    }

    @Test
    fun testSkipNextWhilePlayingLastTrack() = encoreTest { encoreController ->
        val originalState = createActiveState(
            status = PlaybackStatus.Playing,
            queueIndex = 2,
            songs = mediaProvider.getAllSongs().take(3),
        )

        val desiredState = originalState.copy(
            status = PlaybackStatus.Paused(reachedEndOfQueue = true),
            seekPosition = SeekPosition.AbsoluteSeekPosition(30_167)
        )

        encoreController.setStateAndWaitForIdle(originalState)
        encoreController.checkPlaybackStatus(PlaybackStatus.Playing)

        encoreController.skipNext()
        encoreController.waitForStateToSettle()

        assertWithMessage(
            "The player should skip to the end of the track after calling " +
                "skipNext() when the player is playing the last song in the queue"
        )
            .about(mediaPlayerState())
            .that(encoreController.getState())
            .transportState()
            .isEqualTo(desiredState, seekToleranceMs = 0)
    }

    // endregion Test cases

    // region Utility functions

    private inline fun encoreTest(
        crossinline action: suspend (encoreController: EncoreController<FakeSong>) -> Unit
    ) {
        encoreTestRule.withEncore { encoreController ->
            runTest {
                action(encoreController)
            }
        }
    }

    private suspend fun createActiveState(
        status: PlaybackStatus,
        seekPositionMs: Long = 0,
        repeatMode: RepeatMode = RepeatMode.REPEAT_NONE,
        queueIndex: Int = 0,
        songs: List<FakeSong>? = null
    ) = TransportState.Active(
        status = status,
        seekPosition = SeekPosition.AbsoluteSeekPosition(seekPositionMs),
        repeatMode = repeatMode,
        queue = run {
            QueueState.Linear(
                queueIndex = queueIndex,
                queue = (songs ?: mediaProvider.getAllSongs()).map {
                    QueueItem(
                        queueId = UUID.randomUUID(),
                        mediaItem = it
                    )
                }
            )
        }
    )

    private suspend fun EncoreController<FakeSong>.setStateAndWaitForIdle(
        transportState: TransportState<FakeSong>
    ) {
        setState(transportState)
        waitForStateToSettle()
    }

    private suspend fun EncoreController<*>.waitForStateToSettle() {
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        waitForPlayerToBuffer()
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
    }

    private suspend fun EncoreController<*>.waitForPlayerToBuffer() {
        while (isBuffering()) {
            delay(100)
        }
    }

    private suspend fun EncoreController<*>.isBuffering(): Boolean {
        val bufferingState = (getState() as? MediaPlayerState.Prepared<*>)?.bufferingState
        return bufferingState is BufferingState.Buffering && bufferingState.pausedForBuffering
    }

    private suspend fun EncoreController<*>.checkIdle() {
        val state = getState()
        check(state is MediaPlayerState.Ready) {
            "Test setup failed: The Encore service was not in the Idle state"
        }
    }

    private suspend fun EncoreController<*>.checkPlaybackStatus(requiredStatus: PlaybackStatus) {
        val state = getState()
        check(state is MediaPlayerState.Prepared<*>) {
            "Test setup failed: The Encore service was not in the Prepared state"
        }

        val status = state.transportState.status
        check(status == requiredStatus) {
            "Test setup failed: The Encore service's transport status was $status"
        }
    }

    // endregion Utility functions

}
