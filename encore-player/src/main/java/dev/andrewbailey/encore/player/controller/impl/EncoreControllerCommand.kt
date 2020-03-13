package dev.andrewbailey.encore.player.controller.impl

import dev.andrewbailey.encore.player.binder.ServiceHostMessage

internal sealed class EncoreControllerCommand {

    internal class ServiceCommand(
        val message: ServiceHostMessage
    ) : EncoreControllerCommand()

}
