package dev.andrewbailey.encore.player.controller.impl

import android.content.Context
import android.support.v4.media.session.MediaControllerCompat
import dev.andrewbailey.encore.player.MediaPlayerService
import dev.andrewbailey.encore.player.binder.ServiceClientHandler
import dev.andrewbailey.encore.player.binder.ServiceHostMessage
import dev.andrewbailey.encore.player.controller.EncoreController
import dev.andrewbailey.encore.player.controller.EncoreToken
import dev.andrewbailey.encore.player.controller.impl.EncoreControllerCommand.MediaControllerCommand.*
import dev.andrewbailey.encore.player.controller.impl.EncoreControllerCommand.ServiceCommand
import dev.andrewbailey.encore.player.state.TransportState
import dev.andrewbailey.encore.player.util.Resource

internal class EncoreControllerImpl constructor(
    context: Context,
    serviceClass: Class<out MediaPlayerService>
) : EncoreController {

    private val activeTokens = mutableSetOf<EncoreToken>()
    private val clientBinder = ServiceClientBinder(context, serviceClass)

    private val mediaController = Resource<MediaControllerCompat>()

    private val clientHandler: ServiceClientHandler
    private val dispatcher: ServiceControllerDispatcher

    init {
        clientHandler = ServiceClientHandler(
            context = context,
            onSetMediaController = { controller ->
                controller.registerCallback(object : MediaControllerCompat.Callback() {
                    override fun onSessionReady() {
                        mediaController.setResource(controller)
                    }

                    override fun onSessionDestroyed() {
                        if (mediaController.currentResource() == controller) {
                            mediaController.clearResource()
                        }
                    }
                })
            }
        )

        dispatcher = ServiceControllerDispatcher(
            serviceBinder = clientBinder.serviceBinder,
            mediaController = mediaController,
            receiver = clientHandler
        )
    }

    override fun acquireToken(): EncoreToken {
        return EncoreToken().also { token ->
            synchronized(activeTokens) {
                activeTokens += token
                if (activeTokens.size == 1) {
                    connectToService()
                }
            }
        }
    }

    override fun releaseToken(token: EncoreToken) {
        synchronized(activeTokens) {
            if (!activeTokens.remove(token)) {
                throw IllegalStateException("The provided token is not currently registered " +
                        "with this EncoreController instance.")
            }

            if (activeTokens.isEmpty()) {
                disconnectFromService()
            }
        }
    }

    private fun connectToService() {
        clientBinder.bind()
        dispatcher.sendMessage(ServiceCommand(ServiceHostMessage.Initialize))
    }

    private fun disconnectFromService() {
        clientBinder.unbind()
    }

    override fun setState(newState: TransportState) {
        dispatcher.sendMessage(
            ServiceCommand(
                ServiceHostMessage.SetState(
                    newState = newState
                )
            )
        )
    }

    override fun play() {
        dispatcher.sendMessage(Play)
    }

    override fun pause() {
        dispatcher.sendMessage(Pause)
    }

    override fun skipPrevious() {
        dispatcher.sendMessage(SkipPrevious)
    }

    override fun skipNext() {
        dispatcher.sendMessage(SkipNext)
    }

    override fun seekTo(positionMs: Long) {
        dispatcher.sendMessage(SeekTo(positionMs))
    }

}
