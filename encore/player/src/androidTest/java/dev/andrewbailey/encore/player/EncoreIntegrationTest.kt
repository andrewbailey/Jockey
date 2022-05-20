package dev.andrewbailey.encore.player

import androidx.test.espresso.Espresso
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertWithMessage
import dev.andrewbailey.encore.model.QueueItem
import dev.andrewbailey.encore.player.assertions.encore.mediaPlayerState
import dev.andrewbailey.encore.player.controller.EncoreController
import dev.andrewbailey.encore.player.state.BufferingState
import dev.andrewbailey.encore.player.state.MediaPlaybackState
import dev.andrewbailey.encore.player.state.MediaPlayerState
import dev.andrewbailey.encore.player.state.PlaybackStatus
import dev.andrewbailey.encore.player.state.QueueState
import dev.andrewbailey.encore.player.state.RepeatMode
import dev.andrewbailey.encore.player.state.SeekPosition
import dev.andrewbailey.encore.player.state.ShuffleMode
import dev.andrewbailey.encore.player.state.copy
import dev.andrewbailey.encore.player.state.factory.DefaultPlaybackStateFactory
import dev.andrewbailey.encore.player.util.EncoreTestRule
import dev.andrewbailey.encore.player.util.EspressoTimeout
import dev.andrewbailey.encore.test.FakeMusicProvider
import dev.andrewbailey.encore.test.FakeSong
import java.util.Random
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class EncoreIntegrationTest {

    @get:Rule
    val encoreTestRule = EncoreTestRule()

    @get:Rule
    val timeoutRule = EspressoTimeout(10, TimeUnit.SECONDS)

    private lateinit var mediaProvider: FakeMusicProvider
    private val playbackStateFactoryRandomSeed: Long = Random().nextLong()

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().context
        mediaProvider = FakeMusicProvider(context)

        EncoreTestService.Dependencies.apply {
            playbackStateFactoryOverride = DefaultPlaybackStateFactory(
                random = Random(playbackStateFactoryRandomSeed)
            )
        }
    }

    @After
    fun tearDown() {
        EncoreTestService.Dependencies.reset()
    }

    // region Test cases

    @Test
    fun testEncoreInitializesToIdleState() = encoreTest { encoreController ->
        assertWithMessage("The Encore service did not initialize to the expected state")
            .that(encoreController.getState())
            .isEqualTo(
                MediaPlayerState.Ready(
                    mediaPlaybackState = MediaPlaybackState.Empty(
                        repeatMode = RepeatMode.RepeatNone,
                        shuffleMode = ShuffleMode.ShuffleDisabled,
                        playbackSpeed = 1f
                    )
                )
            )
    }

    @Test
    fun testSetStateFromIdleToActiveAndPlaying() = encoreTest { encoreController ->
        val mediaPlaybackState = MediaPlaybackState.Populated(
            status = PlaybackStatus.Playing,
            seekPosition = SeekPosition.AbsoluteSeekPosition(0),
            repeatMode = RepeatMode.RepeatNone,
            queue = QueueState.Linear(
                queueIndex = 0,
                queue = mediaProvider.getAllSongs().map {
                    QueueItem(
                        queueId = UUID.randomUUID(),
                        mediaItem = it
                    )
                }
            ),
            playbackSpeed = 0.01f
        )

        encoreController.setStateAndWaitForIdle(mediaPlaybackState)

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
            .hasSeekPosition(0, thresholdMs = 100)

        assertWithMessage("The player did not have the expected repeat mode")
            .about(mediaPlayerState())
            .that(actualState)
            .transportState()
            .repeatMode()
            .isEqualTo(RepeatMode.RepeatNone)

        assertWithMessage("The player did not have the expected shuffle mode")
            .about(mediaPlayerState())
            .that(actualState)
            .transportState()
            .shuffleMode()
            .isEqualTo(ShuffleMode.ShuffleDisabled)

        assertWithMessage("The player did not have the expected queue state")
            .about(mediaPlayerState())
            .that(actualState)
            .transportState()
            .hasQueueState(mediaPlaybackState.queue)

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
            seekPositionMs = 5000,
            playbackSpeed = 2f
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
        val activeState = createActiveState(
            status = PlaybackStatus.Playing,
            playbackSpeed = 2f
        )

        encoreController.setStateAndWaitForIdle(activeState)

        assertWithMessage("The player was not initialized in the active state")
            .about(mediaPlayerState())
            .that(encoreController.getState())
            .transportState()
            .hasStatus(PlaybackStatus.Playing)

        val emptyState = MediaPlaybackState.Empty(
            repeatMode = RepeatMode.RepeatNone,
            shuffleMode = ShuffleMode.ShuffleDisabled,
            playbackSpeed = 1f
        )

        encoreController.setStateAndWaitForIdle(emptyState)

        assertWithMessage("The player did not return to the idle state")
            .about(mediaPlayerState())
            .that(encoreController.getState())
            .transportState()
            .isEqualTo(emptyState)
    }

    @Test
    fun testPlayWhileIdle() = encoreTest { encoreController ->
        encoreController.checkIdle()

        encoreController.play()
        encoreController.waitForStateToSettle(numberOfMediaSessionCommands = 1)

        assertWithMessage("The player should remain idle after calling play() in the idle state")
            .about(mediaPlayerState())
            .that(encoreController.getState())
            .isEqualTo(
                MediaPlayerState.Ready(
                    mediaPlaybackState = MediaPlaybackState.Empty(
                        repeatMode = RepeatMode.RepeatNone,
                        shuffleMode = ShuffleMode.ShuffleDisabled,
                        playbackSpeed = 1f
                    )
                )
            )
    }

    @Test
    fun testPlayWhilePaused() = encoreTest { encoreController ->
        val originalState = createActiveState(
            status = PlaybackStatus.Paused(),
            playbackSpeed = 0.1f
        )
        val desiredState = originalState.copy(status = PlaybackStatus.Playing)

        encoreController.setStateAndWaitForIdle(originalState)
        encoreController.checkPlaybackStatus(PlaybackStatus.Paused())

        encoreController.play()
        encoreController.waitForStateToSettle(numberOfMediaSessionCommands = 1)

        assertWithMessage("The player should be playing after calling play() in the paused state")
            .about(mediaPlayerState())
            .that(encoreController.getState())
            .transportState()
            .isEqualTo(desiredState, seekToleranceMs = 1000)
    }

    @Test
    fun testPlayWhilePlaying() = encoreTest { encoreController ->
        val originalState = createActiveState(
            status = PlaybackStatus.Playing,
            playbackSpeed = 0.1f
        )
        encoreController.setStateAndWaitForIdle(originalState)
        encoreController.checkPlaybackStatus(PlaybackStatus.Playing)

        encoreController.play()
        encoreController.waitForStateToSettle(numberOfMediaSessionCommands = 1)

        assertWithMessage("The player should do nothing after calling play() when already playing")
            .about(mediaPlayerState())
            .that(encoreController.getState())
            .transportState()
            .isEqualTo(originalState, seekToleranceMs = 1000)
    }

    @Test
    fun testPauseWhileIdle() = encoreTest { encoreController ->
        encoreController.checkIdle()

        encoreController.pause()
        encoreController.waitForStateToSettle(numberOfMediaSessionCommands = 1)

        assertWithMessage("The player should remain idle after calling pause() in the idle state")
            .about(mediaPlayerState())
            .that(encoreController.getState())
            .isEqualTo(
                MediaPlayerState.Ready(
                    mediaPlaybackState = MediaPlaybackState.Empty(
                        repeatMode = RepeatMode.RepeatNone,
                        shuffleMode = ShuffleMode.ShuffleDisabled,
                        playbackSpeed = 1f
                    )
                )
            )
    }

    @Test
    fun testPauseWhilePaused() = encoreTest { encoreController ->
        val originalState = createActiveState(
            status = PlaybackStatus.Paused(),
            playbackSpeed = 1f
        )
        encoreController.setStateAndWaitForIdle(originalState)
        encoreController.checkPlaybackStatus(PlaybackStatus.Paused())

        encoreController.pause()
        encoreController.waitForStateToSettle(numberOfMediaSessionCommands = 1)

        assertWithMessage("The player should do nothing after calling pause() when already paused")
            .about(mediaPlayerState())
            .that(encoreController.getState())
            .transportState()
            .isEqualTo(originalState)
    }

    @Test
    fun testPauseWhilePlaying() = encoreTest { encoreController ->
        val originalState = createActiveState(
            status = PlaybackStatus.Playing,
            playbackSpeed = 0.05f
        )
        val desiredState = originalState.copy(status = PlaybackStatus.Paused())

        encoreController.setStateAndWaitForIdle(originalState)
        encoreController.checkPlaybackStatus(PlaybackStatus.Playing)

        encoreController.pause()
        encoreController.waitForStateToSettle(numberOfMediaSessionCommands = 1)

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
        encoreController.waitForStateToSettle(numberOfMediaSessionCommands = 1)

        assertWithMessage("The player should remain idle after calling skipPrevious() while idle")
            .about(mediaPlayerState())
            .that(encoreController.getState())
            .isEqualTo(
                MediaPlayerState.Ready(
                    mediaPlaybackState = MediaPlaybackState.Empty(
                        repeatMode = RepeatMode.RepeatNone,
                        shuffleMode = ShuffleMode.ShuffleDisabled,
                        playbackSpeed = 1f
                    )
                )
            )
    }

    @Test
    fun testSkipPreviousWhilePausedNearMiddleOfTrack() = encoreTest { encoreController ->
        val originalState = createActiveState(
            status = PlaybackStatus.Paused(),
            queueIndex = 3,
            seekPositionMs = 15_000,
            playbackSpeed = 0.01f
        )

        val desiredState = originalState.copy(
            status = PlaybackStatus.Playing,
            seekPosition = SeekPosition.AbsoluteSeekPosition(0)
        )

        encoreController.setStateAndWaitForIdle(originalState)
        encoreController.checkPlaybackStatus(PlaybackStatus.Paused())

        encoreController.skipPrevious()
        encoreController.waitForStateToSettle(numberOfMediaSessionCommands = 1)

        assertWithMessage(
            "The track should restart when calling skipPrevious() when " +
                "playback is near the middle of the track"
        )
            .about(mediaPlayerState())
            .that(encoreController.getState())
            .transportState()
            .isEqualTo(desiredState, seekToleranceMs = 1000)
    }

    @Test
    fun testSkipPreviousWhilePausedNearBeginningOfTrack() = encoreTest { encoreController ->
        val originalState = createActiveState(
            status = PlaybackStatus.Paused(),
            playbackSpeed = 0.001f,
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
        encoreController.waitForStateToSettle(numberOfMediaSessionCommands = 1)

        assertWithMessage(
            "The play should skip to the previous track after calling " +
                "skipPrevious() when playback is near the middle of the track"
        )
            .about(mediaPlayerState())
            .that(encoreController.getState())
            .transportState()
            .isEqualTo(desiredState, seekToleranceMs = 100)
    }

    @Test
    fun testSkipPreviousWhilePausedNearBeginningOfTrackAndQueue() = encoreTest { encoreController ->
        val originalState = createActiveState(
            status = PlaybackStatus.Paused(),
            playbackSpeed = 0.001f,
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
        encoreController.waitForStateToSettle(numberOfMediaSessionCommands = 1)

        assertWithMessage(
            "The track should restart when calling skipPrevious() when " +
                "playing the first song in the queue"
        )
            .about(mediaPlayerState())
            .that(encoreController.getState())
            .transportState()
            .isEqualTo(desiredState, seekToleranceMs = 100)
    }

    @Test
    fun testSkipPreviousWhilePlayingNearMiddleOfTrack() = encoreTest { encoreController ->
        val originalState = createActiveState(
            status = PlaybackStatus.Playing,
            playbackSpeed = 0.01f,
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
        encoreController.waitForStateToSettle(numberOfMediaSessionCommands = 1)

        assertWithMessage(
            "The track should restart when calling skipPrevious() when " +
                "playback is near the middle of the track"
        )
            .about(mediaPlayerState())
            .that(encoreController.getState())
            .transportState()
            .isEqualTo(desiredState, seekToleranceMs = 1000)
    }

    @Test
    fun testSkipPreviousWhilePlayingNearBeginningOfTrack() = encoreTest { encoreController ->
        val originalState = createActiveState(
            status = PlaybackStatus.Playing,
            playbackSpeed = 0.001f,
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
        encoreController.waitForStateToSettle(numberOfMediaSessionCommands = 1)

        assertWithMessage(
            "The player should skip to the previous track after calling " +
                "skipPrevious() when playback is near the middle of the track"
        )
            .about(mediaPlayerState())
            .that(encoreController.getState())
            .transportState()
            .isEqualTo(desiredState, seekToleranceMs = 100)
    }

    @Test
    fun testSkipNextWhilePlaying() = encoreTest { encoreController ->
        val originalState = createActiveState(
            status = PlaybackStatus.Playing,
            playbackSpeed = 0.001f,
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
        encoreController.waitForStateToSettle(numberOfMediaSessionCommands = 1)

        assertWithMessage(
            "The player should skip to the next track after calling " +
                "skipNext() when the player is playing"
        )
            .about(mediaPlayerState())
            .that(encoreController.getState())
            .transportState()
            .isEqualTo(desiredState, seekToleranceMs = 100)
    }

    @Test
    fun testSkipNextWhilePaused() = encoreTest { encoreController ->
        val originalState = createActiveState(
            status = PlaybackStatus.Paused(),
            playbackSpeed = 0.01f,
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
        encoreController.waitForStateToSettle(numberOfMediaSessionCommands = 1)

        assertWithMessage(
            "The player should skip to the next track after calling " +
                "skipNext() when the player is paused"
        )
            .about(mediaPlayerState())
            .that(encoreController.getState())
            .transportState()
            .isEqualTo(desiredState, seekToleranceMs = 1000)
    }

    @Test
    fun testSkipNextRestartsQueueWithRepeatEnabled() = encoreTest { encoreController ->
        val originalState = createActiveState(
            status = PlaybackStatus.Playing,
            playbackSpeed = 0.01f,
            queueIndex = 2,
            songs = mediaProvider.getAllSongs().take(3),
            repeatMode = RepeatMode.RepeatAll
        )

        val desiredState = originalState.copy(
            status = PlaybackStatus.Playing,
            seekPosition = SeekPosition.AbsoluteSeekPosition(0),
            queue = originalState.queue.copy(queueIndex = 0)
        )

        encoreController.setStateAndWaitForIdle(originalState)
        encoreController.checkPlaybackStatus(PlaybackStatus.Playing)

        encoreController.skipNext()
        encoreController.waitForStateToSettle(numberOfMediaSessionCommands = 1)

        assertWithMessage(
            "The player should restart the queue after calling skipNext() when the " +
                "player is playing the last song in the queue and repeat all is enabled"
        )
            .about(mediaPlayerState())
            .that(encoreController.getState())
            .transportState()
            .isEqualTo(desiredState, seekToleranceMs = 1000)
    }

    @Test
    fun testSkipNextWhilePausedOnLastTrack() = encoreTest { encoreController ->
        val originalState = createActiveState(
            status = PlaybackStatus.Paused(),
            playbackSpeed = 1f,
            queueIndex = 2,
            songs = mediaProvider.getAllSongs().take(3)
        )

        val desiredState = originalState.copy(
            status = PlaybackStatus.Paused(reachedEndOfQueue = true),
            seekPosition = SeekPosition.AbsoluteSeekPosition(30_167)
        )

        encoreController.setStateAndWaitForIdle(originalState)
        encoreController.checkPlaybackStatus(PlaybackStatus.Paused())

        encoreController.skipNext()
        encoreController.waitForStateToSettle(numberOfMediaSessionCommands = 1)

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
            playbackSpeed = 1f,
            queueIndex = 2,
            songs = mediaProvider.getAllSongs().take(3)
        )

        val desiredState = originalState.copy(
            status = PlaybackStatus.Paused(reachedEndOfQueue = true),
            seekPosition = SeekPosition.AbsoluteSeekPosition(30_167)
        )

        encoreController.setStateAndWaitForIdle(originalState)
        encoreController.checkPlaybackStatus(PlaybackStatus.Playing)

        encoreController.skipNext()
        encoreController.waitForStateToSettle(numberOfMediaSessionCommands = 1)

        assertWithMessage(
            "The player should skip to the end of the track after calling " +
                "skipNext() when the player is playing the last song in the queue"
        )
            .about(mediaPlayerState())
            .that(encoreController.getState())
            .transportState()
            .isEqualTo(desiredState, seekToleranceMs = 0)
    }

    @Test
    fun testSetShuffleShufflesTracksWhenPlaying() = encoreTest { encoreController ->
        println("The random seed is $playbackStateFactoryRandomSeed")

        val originalState = createActiveState(
            status = PlaybackStatus.Playing,
            playbackSpeed = 0.001f,
            seekPositionMs = 1500,
            queueIndex = 2
        )

        val nowPlaying = originalState.queue.nowPlaying
        val desiredState = originalState.copy(
            queue = QueueState.Shuffled(
                queue = buildList {
                    add(nowPlaying)
                    addAll(
                        originalState.queue.queue
                            .filter { it != nowPlaying }
                            .shuffled(Random(playbackStateFactoryRandomSeed))
                    )
                },
                queueIndex = 0,
                linearQueue = originalState.queue.queue
            )
        )

        encoreController.setStateAndWaitForIdle(originalState)
        encoreController.checkPlaybackStatus(PlaybackStatus.Playing)

        encoreController.setShuffleMode(ShuffleMode.ShuffleEnabled)
        encoreController.waitForStateToSettle(numberOfMediaSessionCommands = 1)

        val actualState = encoreController.getState()
        assertWithMessage("The player did not shuffle its tracks as expected")
            .about(mediaPlayerState())
            .that(actualState)
            .transportState()
            .hasQueueState(desiredState.queue)

        assertWithMessage(
            "The final state after enabling shuffling mode did not match the expected value"
        )
            .about(mediaPlayerState())
            .that(actualState)
            .transportState()
            .isEqualTo(desiredState, seekToleranceMs = 100)
    }

    @Test
    fun testSetShuffleShufflesTracksWhenPaused() = encoreTest { encoreController ->
        println("The random seed is $playbackStateFactoryRandomSeed")

        val originalState = createActiveState(
            status = PlaybackStatus.Paused(),
            playbackSpeed = 0.001f,
            seekPositionMs = 1500,
            queueIndex = 2
        )

        val nowPlaying = originalState.queue.nowPlaying
        val desiredState = originalState.copy(
            queue = QueueState.Shuffled(
                queue = buildList {
                    add(nowPlaying)
                    addAll(
                        originalState.queue.queue
                            .filter { it != nowPlaying }
                            .shuffled(Random(playbackStateFactoryRandomSeed))
                    )
                },
                queueIndex = 0,
                linearQueue = originalState.queue.queue
            )
        )

        encoreController.setStateAndWaitForIdle(originalState)
        encoreController.checkPlaybackStatus(PlaybackStatus.Paused())

        encoreController.setShuffleMode(ShuffleMode.ShuffleEnabled)
        encoreController.waitForStateToSettle(numberOfMediaSessionCommands = 1)

        val actualState = encoreController.getState()
        assertWithMessage("The player did not shuffle its tracks as expected")
            .about(mediaPlayerState())
            .that(actualState)
            .transportState()
            .hasQueueState(desiredState.queue)

        assertWithMessage(
            "The final state after enabling shuffling mode did not match the expected value"
        )
            .about(mediaPlayerState())
            .that(actualState)
            .transportState()
            .isEqualTo(desiredState, seekToleranceMs = 100)
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
        playbackSpeed: Float,
        seekPositionMs: Long = 0,
        repeatMode: RepeatMode = RepeatMode.RepeatNone,
        queueIndex: Int = 0,
        songs: List<FakeSong>? = null
    ) = MediaPlaybackState.Populated(
        status = status,
        seekPosition = SeekPosition.AbsoluteSeekPosition(seekPositionMs),
        repeatMode = repeatMode,
        playbackSpeed = playbackSpeed,
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
        mediaPlaybackState: MediaPlaybackState<FakeSong>
    ) {
        setState(mediaPlaybackState)
        waitForStateToSettle()
    }

    private suspend fun EncoreController<*>.waitForStateToSettle(
        numberOfMediaSessionCommands: Int? = null
    ) {
        numberOfMediaSessionCommands?.let {
            encoreTestRule.setNumberOfExpectedMediaSessionCommands(it)
        }
        Espresso.onIdle()
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
            "Test setup failed: The Encore service was not in the Idle state " +
                "(it was actually $state)"
        }
    }

    private suspend fun EncoreController<*>.checkPlaybackStatus(requiredStatus: PlaybackStatus) {
        val state = getState()
        check(state is MediaPlayerState.Prepared<*>) {
            "Test setup failed: The Encore service was not in the Prepared state " +
                "(it was actually $state)"
        }

        val status = state.mediaPlaybackState.status
        check(status == requiredStatus) {
            "Test setup failed: The Encore service's transport status was $status"
        }
    }

    // endregion Utility functions

}
