package dev.andrewbailey.encore.player.controller.impl

import android.content.Context
import dev.andrewbailey.encore.player.MediaPlayerService
import dev.andrewbailey.encore.player.binder.ServiceClientHandler
import dev.andrewbailey.encore.player.binder.ServiceHostMessage
import dev.andrewbailey.encore.player.controller.EncoreController
import dev.andrewbailey.encore.player.controller.EncoreToken
import dev.andrewbailey.encore.player.state.TransportState

internal class EncoreControllerImpl constructor(
    context: Context,
    serviceClass: Class<out MediaPlayerService>
) : EncoreController {

    private val activeTokens = mutableSetOf<EncoreToken>()
    private val clientBinder = ServiceClientBinder(context, serviceClass)

    private val clientHandler: ServiceClientHandler
    private val dispatcher: ServiceControllerDispatcher

    init {
        clientHandler = ServiceClientHandler(
        )

        dispatcher = ServiceControllerDispatcher(
            serviceBinder = clientBinder.serviceBinder,
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
    }

    private fun disconnectFromService() {
        clientBinder.unbind()
    }

    override fun setState(newState: TransportState) {
        dispatcher.sendMessage(
            EncoreControllerCommand.ServiceCommand(
                ServiceHostMessage.SetState(
                    newState = newState
                )
            )
        )
    }
}
