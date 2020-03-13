package dev.andrewbailey.encore.player.binder

import dev.andrewbailey.ipc.BidirectionalMessenger
import dev.andrewbailey.ipc.bidirectionalMessenger

internal typealias ServiceBidirectionalMessenger =
        BidirectionalMessenger<ServiceHostMessage, ServiceClientMessage>

internal class ServiceHostHandler(
) : PlaybackObserver {

    override fun onPlaybackStateChanged(newState: MediaPlayerState) {
    }

}
