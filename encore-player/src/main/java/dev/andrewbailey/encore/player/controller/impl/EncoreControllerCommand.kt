package dev.andrewbailey.encore.player.controller.impl

import dev.andrewbailey.encore.player.binder.ServiceHostMessage

internal sealed class EncoreControllerCommand {

    internal class ServiceCommand(
        val message: ServiceHostMessage
    ) : EncoreControllerCommand()

    internal sealed class MediaControllerCommand : EncoreControllerCommand() {

        object Play : MediaControllerCommand()

        object Pause : MediaControllerCommand()

        object SkipPrevious : MediaControllerCommand()

        object SkipNext : MediaControllerCommand()

        class SeekTo(
            val positionMs: Long
        ) : MediaControllerCommand()

    }

}