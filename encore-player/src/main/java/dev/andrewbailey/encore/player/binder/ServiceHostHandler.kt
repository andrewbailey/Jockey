package dev.andrewbailey.encore.player.binder

import dev.andrewbailey.encore.player.playback.PlaybackObserver
import dev.andrewbailey.encore.player.state.MediaPlayerState
import dev.andrewbailey.encore.player.state.TransportState
import dev.andrewbailey.ipc.BidirectionalMessenger
import dev.andrewbailey.ipc.bidirectionalMessenger

internal typealias ServiceBidirectionalMessenger =
        BidirectionalMessenger<ServiceHostMessage, ServiceClientMessage>

internal class ServiceHostHandler(
    private val onSetState: (TransportState) -> Unit
) : PlaybackObserver {

    val messenger: ServiceBidirectionalMessenger = bidirectionalMessenger { data, replyTo ->
        when (data) {
            is ServiceHostMessage.SetState -> {
                onSetState(data.newState)
            }
        }.let { /* Require exhaustive when */ }
    }
    override fun onPlaybackStateChanged(newState: MediaPlayerState) {
    }

}
