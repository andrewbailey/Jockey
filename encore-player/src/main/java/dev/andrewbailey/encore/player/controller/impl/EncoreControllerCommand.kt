package dev.andrewbailey.encore.player.controller.impl

import dev.andrewbailey.encore.model.MediaItem
import dev.andrewbailey.encore.player.binder.ServiceHostMessage
import dev.andrewbailey.encore.player.state.ShuffleMode

internal sealed class EncoreControllerCommand<out M : MediaItem> {

    internal class ServiceCommand<out M : MediaItem>(
        val message: ServiceHostMessage<M>
    ) : EncoreControllerCommand<M>()

    internal sealed class MediaControllerCommand : EncoreControllerCommand<Nothing>() {

        object Play : MediaControllerCommand()

        object Pause : MediaControllerCommand()

        object SkipPrevious : MediaControllerCommand()

        object SkipNext : MediaControllerCommand()

        class SeekTo(
            val positionMs: Long
        ) : MediaControllerCommand()

        class SetShuffleMode(
            val shuffleMode: ShuffleMode
        ) : MediaControllerCommand()

    }

}
