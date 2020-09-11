package dev.andrewbailey.encore.player.controller

import android.content.Context
import dev.andrewbailey.encore.model.MediaItem
import dev.andrewbailey.encore.player.MediaPlayerService
import dev.andrewbailey.encore.player.controller.impl.EncoreControllerImpl
import dev.andrewbailey.encore.player.state.MediaPlayerState
import dev.andrewbailey.encore.player.state.ShuffleMode
import dev.andrewbailey.encore.player.state.TransportState
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.Flow

public interface EncoreController<M : MediaItem> {

    public fun acquireToken(): EncoreToken

    public fun releaseToken(token: EncoreToken)

    public fun observeState(
        seekUpdateFrequency: SeekUpdateFrequency = SeekUpdateFrequency.Never
    ): Flow<MediaPlayerState<M>>

    public suspend fun getState(): MediaPlayerState<M>

    public fun setState(newState: TransportState<M>)

    public fun play()

    public fun pause()

    public fun skipPrevious()

    public fun skipNext()

    public fun seekTo(positionMs: Long)

    public fun setShuffleMode(shuffleMode: ShuffleMode)

    public companion object {
        public fun <M : MediaItem> create(
            context: Context,
            serviceClass: Class<out MediaPlayerService<M>>
        ): EncoreController<M> {
            return EncoreControllerImpl(
                context = context.applicationContext,
                serviceClass = serviceClass
            )
        }
    }

    public sealed class SeekUpdateFrequency {
        public object Never : SeekUpdateFrequency()

        public data class WhilePlayingEvery(
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
