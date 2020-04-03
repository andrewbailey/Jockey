package dev.andrewbailey.encore.player.controller.impl

import android.os.DeadObjectException
import android.support.v4.media.session.MediaControllerCompat
import android.util.Log
import dev.andrewbailey.encore.player.binder.ServiceBidirectionalMessenger
import dev.andrewbailey.encore.player.binder.ServiceClientHandler
import dev.andrewbailey.encore.player.controller.impl.EncoreControllerCommand.MediaControllerCommand
import dev.andrewbailey.encore.player.controller.impl.EncoreControllerCommand.ServiceCommand
import dev.andrewbailey.encore.player.util.Resource
import java.util.concurrent.Executors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED
import kotlinx.coroutines.launch

internal class ServiceControllerDispatcher(
    private val serviceBinder: Resource<ServiceBidirectionalMessenger>,
    private val mediaController: Resource<MediaControllerCompat>,
    private val receiver: ServiceClientHandler
) {

    private val executor = Executors.newFixedThreadPool(1)
    private val dispatchScope = CoroutineScope(executor.asCoroutineDispatcher())
    private val messageQueue = Channel<EncoreControllerCommand>(capacity = UNLIMITED)

    init {
        dispatchScope.launch {
            dispatchLoop()
        }
    }

    fun sendMessage(message: EncoreControllerCommand) {
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
                is ServiceCommand -> handleMessage(message)
                is MediaControllerCommand -> handleMessage(message)
            }
        }
    }

    private suspend fun handleMessage(command: ServiceCommand) {
        while (true) {
            val sender = serviceBinder.getResource { it.isAlive }

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
        val controller = mediaController.getResource().transportControls

        when (message) {
            MediaControllerCommand.Play -> controller.play()
            MediaControllerCommand.Pause -> controller.pause()
            MediaControllerCommand.SkipPrevious -> controller.skipToPrevious()
            MediaControllerCommand.SkipNext -> controller.skipToNext()
            is MediaControllerCommand.SeekTo -> controller.seekTo(message.positionMs)
        }.let { /* Require exhaustive when */ }
    }

}
