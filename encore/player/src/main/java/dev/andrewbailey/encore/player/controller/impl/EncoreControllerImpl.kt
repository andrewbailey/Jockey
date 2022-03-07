package dev.andrewbailey.encore.player.controller.impl

import android.content.ComponentName
import android.content.Context
import android.support.v4.media.session.MediaControllerCompat
import androidx.annotation.VisibleForTesting
import androidx.annotation.VisibleForTesting.PRIVATE
import dev.andrewbailey.encore.model.MediaObject
import dev.andrewbailey.encore.player.binder.ServiceClientHandler
import dev.andrewbailey.encore.player.binder.ServiceHostMessage
import dev.andrewbailey.encore.player.controller.EncoreController
import dev.andrewbailey.encore.player.controller.EncoreController.SeekUpdateFrequency
import dev.andrewbailey.encore.player.controller.EncoreToken
import dev.andrewbailey.encore.player.controller.impl.EncoreControllerCommand.MediaControllerCommand.Pause
import dev.andrewbailey.encore.player.controller.impl.EncoreControllerCommand.MediaControllerCommand.Play
import dev.andrewbailey.encore.player.controller.impl.EncoreControllerCommand.MediaControllerCommand.SeekTo
import dev.andrewbailey.encore.player.controller.impl.EncoreControllerCommand.MediaControllerCommand.SetShuffleMode
import dev.andrewbailey.encore.player.controller.impl.EncoreControllerCommand.MediaControllerCommand.SkipNext
import dev.andrewbailey.encore.player.controller.impl.EncoreControllerCommand.MediaControllerCommand.SkipPrevious
import dev.andrewbailey.encore.player.controller.impl.EncoreControllerCommand.ServiceCommand
import dev.andrewbailey.encore.player.state.BufferingState.Buffering
import dev.andrewbailey.encore.player.state.MediaPlayerState
import dev.andrewbailey.encore.player.state.PlaybackState.PLAYING
import dev.andrewbailey.encore.player.state.ShuffleMode
import dev.andrewbailey.encore.player.state.TransportState
import dev.andrewbailey.encore.player.state.diff.MediaPlayerStateDiffer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal class EncoreControllerImpl<M : MediaObject> constructor(
    context: Context,
    componentName: ComponentName
) : EncoreController<M> {

    private val bindMutex = Mutex()

    private val activeTokens = mutableSetOf<EncoreToken>()

    @VisibleForTesting(otherwise = PRIVATE)
    val clientBinder = ServiceClientBinder<M>(context, componentName)

    private val playbackState = MutableStateFlow<MediaPlayerState<M>?>(null)
    private val mediaController = MutableStateFlow<MediaControllerCompat?>(null)
    private val stateDiffer = MediaPlayerStateDiffer<M>()

    private val clientHandler: ServiceClientHandler<M>
    private val dispatcher: ServiceControllerDispatcher<M>

    init {
        clientHandler = ServiceClientHandler(
            context = context,
            onSetMediaController = { controller ->
                controller.registerCallback(
                    object : MediaControllerCompat.Callback() {
                        override fun onSessionReady() {
                            mediaController.value = controller
                        }

                        override fun onSessionDestroyed() {
                            if (mediaController.value == controller) {
                                mediaController.value = null
                            }
                        }
                    }
                )
            },
            onSetMediaPlayerState = { playbackState.value = it },
            onApplyMediaStateDiff = { diff ->
                val currentState = checkNotNull(playbackState.value) {
                    "Cannot apply a state diff, because no pre-existing state is available locally"
                }

                check(currentState is MediaPlayerState.Prepared) {
                    "Cannot apply a state diff, because the previous value wasn't in the " +
                        "Prepared state."
                }

                playbackState.value = stateDiffer.applyDiff(currentState, diff)
            }
        )

        dispatcher = ServiceControllerDispatcher(
            serviceBinder = clientBinder.serviceBinder,
            mediaController = mediaController,
            receiver = clientHandler
        )
    }

    override fun acquireToken(): EncoreToken {
        val token = EncoreToken()
        CoroutineScope(Dispatchers.IO).launch {
            bindMutex.withLock(token) {
                if (activeTokens.isEmpty()) {
                    connectToService()
                }
                activeTokens += token
            }
        }

        return token
    }

    override fun releaseToken(token: EncoreToken) {
        CoroutineScope(Dispatchers.IO).launch {
            bindMutex.withLock(token) {
                if (activeTokens.remove(token) && activeTokens.isEmpty()) {
                    disconnectFromService()
                }
            }
        }
    }

    private suspend fun connectToService() {
        clientBinder.bind()
        dispatcher.sendMessage(ServiceCommand(ServiceHostMessage.Initialize))
    }

    private fun disconnectFromService() {
        clientBinder.unbind()
    }

    override fun observeState(
        seekUpdateFrequency: SeekUpdateFrequency
    ): Flow<MediaPlayerState<M>> {
        return playbackState
            .filterNotNull()
            .flatMapLatest { state ->
                when (seekUpdateFrequency) {
                    is SeekUpdateFrequency.Never -> {
                        flowOf(state)
                    }
                    is SeekUpdateFrequency.WhilePlayingEvery -> {
                        resendEveryInterval(state, seekUpdateFrequency.intervalMs)
                    }
                }
            }
    }

    override suspend fun getState(): MediaPlayerState<M> {
        return playbackState.filterNotNull().first()
    }

    private fun resendEveryInterval(
        state: MediaPlayerState<M>,
        intervalMs: Long
    ): Flow<MediaPlayerState<M>> {
        return flow {
            val duration = (state as? MediaPlayerState.Prepared)
                ?.takeIf { it.transportState.status == PLAYING }
                ?.takeIf { (it.bufferingState as? Buffering)?.pausedForBuffering != true }
                ?.durationMs

            val seekPosition = (state as? MediaPlayerState.Prepared)
                ?.transportState
                ?.seekPosition

            var seekPositionMs: Long?
            do {
                seekPositionMs = seekPosition?.seekPositionMillis
                emit(state)
                delay(intervalMs)
            } while (duration != null && seekPositionMs != null &&
                seekPositionMs < duration
            )
        }
    }

    override suspend fun setState(newState: TransportState<M>) {
        dispatcher.sendMessage(
            ServiceCommand(
                ServiceHostMessage.SetState(
                    newState = newState
                )
            )
        )
    }

    override suspend fun play() {
        dispatcher.sendMessage(Play)
    }

    override suspend fun pause() {
        this.dispatcher.sendMessage(Pause)
    }

    override suspend fun skipPrevious() {
        dispatcher.sendMessage(SkipPrevious)
    }

    override suspend fun skipNext() {
        dispatcher.sendMessage(SkipNext)
    }

    override suspend fun seekTo(positionMs: Long) {
        dispatcher.sendMessage(SeekTo(positionMs))
    }

    override suspend fun setShuffleMode(shuffleMode: ShuffleMode) {
        dispatcher.sendMessage(SetShuffleMode(shuffleMode))
    }

}
