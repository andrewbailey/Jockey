package dev.andrewbailey.encore.player.controller

import android.content.Context
import dev.andrewbailey.encore.player.MediaPlayerService
import dev.andrewbailey.encore.player.controller.impl.EncoreControllerImpl
import dev.andrewbailey.encore.player.state.MediaPlayerState
import dev.andrewbailey.encore.player.state.ShuffleMode
import dev.andrewbailey.encore.player.state.TransportState
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.Flow

interface EncoreController {

    fun acquireToken(): EncoreToken

    fun releaseToken(token: EncoreToken)

    fun observeState(
        seekUpdateFrequency: SeekUpdateFrequency = SeekUpdateFrequency.Never
    ): Flow<MediaPlayerState>

    suspend fun getState(): MediaPlayerState

    fun setState(newState: TransportState)

    fun play()

    fun pause()

    fun skipPrevious()

    fun skipNext()

    fun seekTo(positionMs: Long)

    fun setShuffleMode(shuffleMode: ShuffleMode)

    companion object {
        inline fun <reified T : MediaPlayerService> create(context: Context) =
            create(context, T::class.java)

        fun create(
            context: Context,
            serviceClass: Class<out MediaPlayerService>
        ): EncoreController {
            return EncoreControllerImpl(
                context = context.applicationContext,
                serviceClass = serviceClass
            )
        }
    }

    sealed class SeekUpdateFrequency {
        object Never : SeekUpdateFrequency()

        data class WhilePlayingEvery(
            val interval: Long,
            val timeUnit: TimeUnit
        ) : SeekUpdateFrequency() {

            internal val intervalMs: Long
                get() = timeUnit.toMillis(interval)

            init {
                require(interval > 0) {
                    "Interval must be greater than 0."
                }
            }
        }
    }

}
