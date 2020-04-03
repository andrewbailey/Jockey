package dev.andrewbailey.encore.player.binder

import android.content.Context
import android.support.v4.media.session.MediaControllerCompat
import dev.andrewbailey.ipc.BidirectionalMessenger
import dev.andrewbailey.ipc.bidirectionalMessenger

internal typealias ClientBidirectionalMessenger =
        BidirectionalMessenger<ServiceClientMessage, ServiceHostMessage>

internal class ServiceClientHandler(
    private val context: Context,
    private val onSetMediaController: (MediaControllerCompat) -> Unit
) {

    val messenger: ClientBidirectionalMessenger = bidirectionalMessenger { data, replyTo ->
        when (data) {
            is ServiceClientMessage.Initialize -> {
                onSetMediaController(MediaControllerCompat(context, data.mediaSessionToken))
            }
        }.let { /* Require exhaustive let */ }
    }

}
