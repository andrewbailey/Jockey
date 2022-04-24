package dev.andrewbailey.encore.player.controller.impl

import android.os.DeadObjectException
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaControllerCompat.TransportControls
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import dev.andrewbailey.encore.model.MediaObject
import dev.andrewbailey.encore.player.binder.ServiceBidirectionalMessenger
import dev.andrewbailey.encore.player.binder.ServiceClientHandler
import dev.andrewbailey.encore.player.controller.impl.EncoreControllerCommand.MediaControllerCommand
import dev.andrewbailey.encore.player.controller.impl.EncoreControllerCommand.MediaControllerCommand.Pause
import dev.andrewbailey.encore.player.controller.impl.EncoreControllerCommand.MediaControllerCommand.Play
import dev.andrewbailey.encore.player.controller.impl.EncoreControllerCommand.MediaControllerCommand.SeekTo
import dev.andrewbailey.encore.player.controller.impl.EncoreControllerCommand.MediaControllerCommand.SetShuffleMode
import dev.andrewbailey.encore.player.controller.impl.EncoreControllerCommand.MediaControllerCommand.SkipNext
import dev.andrewbailey.encore.player.controller.impl.EncoreControllerCommand.MediaControllerCommand.SkipPrevious
import dev.andrewbailey.encore.player.controller.impl.EncoreControllerCommand.ServiceCommand
import dev.andrewbailey.encore.player.state.ShuffleMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

internal class ServiceControllerDispatcher<M : MediaObject> constructor(
    private val serviceBinder: StateFlow<ServiceBidirectionalMessenger<M>?>,
    private val mediaController: StateFlow<MediaControllerCompat?>,
    private val receiver: ServiceClientHandler<M>
) {

    private val commandMutex = Mutex()

    suspend fun sendMessage(message: EncoreControllerCommand<M>) {
        withContext(Dispatchers.IO) {
            commandMutex.withLock(owner = message) {
                when (message) {
                    is ServiceCommand<M> -> handleMessage(message)
                    is MediaControllerCommand -> handleMessage(message)
                }
            }
        }
    }

    private suspend fun handleMessage(command: ServiceCommand<M>) {
        while (true) {
            val sender = serviceBinder.filterNotNull().filter { it.isAlive }.first()

            try {
                sender.send(command.message, receiver.messenger)
                return
            } catch (exception: DeadObjectException) {
                Log.e(
                    "EncoreControllerImpl",
                    "The service has disconnected while attempting to send a message. We will " +
                        "attempt to resend it once the connection is restored.",
                    exception
                )
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
        setShuffleMode(
            when (shuffleMode) {
                ShuffleMode.ShuffleDisabled -> PlaybackStateCompat.SHUFFLE_MODE_NONE
                ShuffleMode.ShuffleEnabled -> PlaybackStateCompat.SHUFFLE_MODE_ALL
            }
        )
    }

}
