package dev.andrewbailey.encore.player.binder

import android.content.Context
import android.support.v4.media.session.MediaControllerCompat
import dev.andrewbailey.encore.player.state.MediaPlayerState
import dev.andrewbailey.ipc.BidirectionalMessenger
import dev.andrewbailey.ipc.bidirectionalMessenger

internal typealias ClientBidirectionalMessenger =
        BidirectionalMessenger<ServiceClientMessage, ServiceHostMessage>

internal class ServiceClientHandler(
    private val context: Context,
    private val onSetMediaController: (MediaControllerCompat) -> Unit,
    private val onSetMediaPlayerState: (MediaPlayerState) -> Unit
) {

    val messenger: ClientBidirectionalMessenger = bidirectionalMessenger { data, replyTo ->
        when (data) {
            is ServiceClientMessage.Initialize -> {
                onSetMediaController(MediaControllerCompat(context, data.mediaSessionToken))
                onSetMediaPlayerState(data.firstState)
            }
            is ServiceClientMessage.UpdateState -> {
                onSetMediaPlayerState(data.newState)
            }
        }.let { /* Require exhaustive let */ }
    }

}
