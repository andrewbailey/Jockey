package dev.andrewbailey.encore.player.controller.impl

import android.os.DeadObjectException
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaControllerCompat.*
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import dev.andrewbailey.encore.model.MediaObject
import dev.andrewbailey.encore.player.binder.ServiceBidirectionalMessenger
import dev.andrewbailey.encore.player.binder.ServiceClientHandler
import dev.andrewbailey.encore.player.controller.impl.EncoreControllerCommand.MediaControllerCommand
import dev.andrewbailey.encore.player.controller.impl.EncoreControllerCommand.MediaControllerCommand.*
import dev.andrewbailey.encore.player.controller.impl.EncoreControllerCommand.ServiceCommand
import dev.andrewbailey.encore.player.state.ShuffleMode
import java.util.concurrent.Executors
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first

internal class ServiceControllerDispatcher<M : MediaObject> constructor(
    private val serviceBinder: StateFlow<ServiceBidirectionalMessenger<M>?>,
    private val mediaController: StateFlow<MediaControllerCompat?>,
    private val receiver: ServiceClientHandler<M>
) {

    private val executor = Executors.newFixedThreadPool(1)
    private val dispatchScope = CoroutineScope(executor.asCoroutineDispatcher())
    private val messageQueue = Channel<EncoreControllerCommand<M>>(capacity = UNLIMITED)

    init {
        dispatchScope.launch {
            dispatchLoop()
        }
    }

    fun sendMessage(message: EncoreControllerCommand<M>) {
        if (!messageQueue.offer(message)) {
            throw RuntimeException("Failed to enqueue message to be dispatched.")
        }
    }

    fun cancelDispatch() {
        dispatchScope.cancel(message = "The service controller has been released.")
    }

    private suspend fun dispatchLoop(): Nothing {
        while (true) {
            when (val message = messageQueue.receive()) {
                is ServiceCommand<M> -> handleMessage(message)
                is MediaControllerCommand -> handleMessage(message)
            }
        }
    }

    private suspend fun handleMessage(command: ServiceCommand<M>) {
        while (true) {
            val sender = serviceBinder.filterNotNull().filter { it.isAlive }.first()

            try {
                sender.send(command.message, receiver.messenger)
                return
            } catch (e: DeadObjectException) {
                Log.e("EncoreControllerImpl", "The service has disconnected while attempting " +
                        "to send a message. We will attempt to resend it once the connection " +
                        "is restored.", e)
            }
        }
    }

    private suspend fun handleMessage(message: MediaControllerCommand) {
        val controller = mediaController.filterNotNull().first().transportControls

        when (message) {
            Play -> controller.play()
            Pause -> controller.pause()
            SkipPrevious -> controller.skipToPrevious()
            SkipNext -> controller.skipToNext()
            is SeekTo -> controller.seekTo(message.positionMs)
            is SetShuffleMode -> controller.setShuffleMode(message.shuffleMode)
        }.let { /* Require exhaustive when */ }
    }

    private fun TransportControls.setShuffleMode(shuffleMode: ShuffleMode) {
        setShuffleMode(when (shuffleMode) {
            ShuffleMode.LINEAR -> PlaybackStateCompat.SHUFFLE_MODE_NONE
            ShuffleMode.SHUFFLED -> PlaybackStateCompat.SHUFFLE_MODE_ALL
        })
    }

}
