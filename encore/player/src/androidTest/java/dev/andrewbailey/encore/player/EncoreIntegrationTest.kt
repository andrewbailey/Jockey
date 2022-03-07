package dev.andrewbailey.encore.player

import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertWithMessage
import dev.andrewbailey.encore.player.controller.EncoreController
import dev.andrewbailey.encore.player.state.MediaPlayerState
import dev.andrewbailey.encore.player.state.RepeatMode
import dev.andrewbailey.encore.player.state.ShuffleMode
import dev.andrewbailey.encore.player.state.TransportState
import dev.andrewbailey.encore.player.util.EncoreTestRule
import dev.andrewbailey.encore.test.FakeMusicProvider
import dev.andrewbailey.encore.test.FakeSong
import java.util.concurrent.TimeUnit
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

    private inline fun encoreTest(
        crossinline action: suspend (encoreController: EncoreController<FakeSong>) -> Unit
    ) {
        encoreTestRule.withEncore { encoreController ->
            runTest {
                action(encoreController)
            }
        }
    }

}
