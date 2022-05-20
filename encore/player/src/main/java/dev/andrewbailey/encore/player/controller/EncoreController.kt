package dev.andrewbailey.encore.player.controller

import android.content.ComponentName
import android.content.Context
import dev.andrewbailey.encore.model.MediaObject
import dev.andrewbailey.encore.player.MediaPlayerService
import dev.andrewbailey.encore.player.controller.impl.EncoreControllerImpl
import dev.andrewbailey.encore.player.state.MediaPlaybackState
import dev.andrewbailey.encore.player.state.MediaPlayerState
import dev.andrewbailey.encore.player.state.ShuffleMode
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.Flow

public interface EncoreController<M : MediaObject> {

    public fun acquireToken(): EncoreToken

    public fun releaseToken(token: EncoreToken)

    public fun observeState(
        seekUpdateFrequency: SeekUpdateFrequency = SeekUpdateFrequency.Never
    ): Flow<MediaPlayerState<M>>

    public suspend fun getState(): MediaPlayerState<M>

    public suspend fun setState(newState: MediaPlaybackState<M>)

    public suspend fun play()

    public suspend fun pause()

    public suspend fun skipPrevious()

    public suspend fun skipNext()

    public suspend fun seekTo(positionMs: Long)

    public suspend fun setShuffleMode(shuffleMode: ShuffleMode)

    public companion object {
        public fun <M : MediaObject> create(
            context: Context,
            serviceClass: Class<out MediaPlayerService<M>>
        ): EncoreController<M> {
            return EncoreControllerImpl(
                context = context.applicationContext,
                componentName = ComponentName(context.applicationContext, serviceClass)
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
