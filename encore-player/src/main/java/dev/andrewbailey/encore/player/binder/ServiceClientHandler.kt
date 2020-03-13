package dev.andrewbailey.encore.player.binder

import dev.andrewbailey.ipc.BidirectionalMessenger
import dev.andrewbailey.ipc.bidirectionalMessenger

internal typealias ClientBidirectionalMessenger =
        BidirectionalMessenger<ServiceClientMessage, ServiceHostMessage>

internal class ServiceClientHandler(
) {

    val messenger: ClientBidirectionalMessenger = bidirectionalMessenger { data, replyTo ->
    }

}
