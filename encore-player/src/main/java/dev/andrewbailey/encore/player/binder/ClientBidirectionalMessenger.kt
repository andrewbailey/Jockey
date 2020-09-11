package dev.andrewbailey.encore.player.binder

import android.os.IBinder
import dev.andrewbailey.encore.model.MediaItem
import dev.andrewbailey.ipc.BidirectionalMessenger

internal inline class ClientBidirectionalMessenger<M : MediaItem>(
    val messenger: BidirectionalMessenger<ServiceClientMessage<M>, ServiceHostMessage<M>>
) {

    val isAlive: Boolean
        get() = messenger.isAlive

    val binder: IBinder
        get() = messenger.binder

    @Suppress("NOTHING_TO_INLINE")
    inline fun send(
        message: ServiceClientMessage<M>,
        respondTo: ServiceBidirectionalMessenger<M>
    ) {
        messenger.send(message, respondTo.messenger)
    }
}
