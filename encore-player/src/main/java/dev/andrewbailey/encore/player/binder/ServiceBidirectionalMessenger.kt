package dev.andrewbailey.encore.player.binder

import android.os.IBinder
import dev.andrewbailey.encore.model.MediaItem
import dev.andrewbailey.ipc.BidirectionalMessenger
import dev.andrewbailey.ipc.bidirectionalMessenger

internal inline class ServiceBidirectionalMessenger<M : MediaItem>(
    val messenger: BidirectionalMessenger<ServiceHostMessage<M>, ServiceClientMessage<M>>
) {

    constructor(service: IBinder) : this(bidirectionalMessenger(service))

    val isAlive: Boolean
        get() = messenger.isAlive

    val binder: IBinder
        get() = messenger.binder

    @Suppress("NOTHING_TO_INLINE")
    inline fun send(
        message: ServiceHostMessage<M>,
        respondTo: ClientBidirectionalMessenger<M>
    ) {
        messenger.send(message, respondTo.messenger)
    }
}
