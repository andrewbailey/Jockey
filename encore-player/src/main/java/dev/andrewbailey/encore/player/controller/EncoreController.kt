package dev.andrewbailey.encore.player.controller

import android.content.Context
import dev.andrewbailey.encore.player.MediaPlayerService
import dev.andrewbailey.encore.player.controller.impl.EncoreControllerImpl
import dev.andrewbailey.encore.player.state.TransportState

interface EncoreController {

    fun acquireToken(): EncoreToken

    fun releaseToken(token: EncoreToken)

    fun setState(newState: TransportState)

    companion object {
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

}
