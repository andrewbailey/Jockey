package dev.andrewbailey.encore.player.binder

import android.os.DeadObjectException
import android.support.v4.media.session.MediaSessionCompat
import dev.andrewbailey.encore.player.playback.PlaybackObserver
import dev.andrewbailey.encore.player.state.MediaPlayerState
import dev.andrewbailey.encore.player.state.TransportState
import dev.andrewbailey.ipc.BidirectionalMessenger
import dev.andrewbailey.ipc.bidirectionalMessenger

internal typealias ServiceBidirectionalMessenger =
        BidirectionalMessenger<ServiceHostMessage, ServiceClientMessage>

internal class ServiceHostHandler(
    private val getState: () -> MediaPlayerState,
    private val getMediaSession: () -> MediaSessionCompat,
    private val onSetState: (TransportState) -> Unit
) : PlaybackObserver {

    private val subscribers = mutableListOf<ClientBidirectionalMessenger>()

    val messenger: ServiceBidirectionalMessenger = bidirectionalMessenger { data, replyTo ->
        when (data) {
            is ServiceHostMessage.SetState -> {
                onSetState(data.newState)
            }
            ServiceHostMessage.Initialize -> {
                subscribers += replyTo
                replyTo.send(
                    ServiceClientMessage.Initialize(
                        firstState = getState(),
                        mediaSessionToken = getMediaSession().sessionToken
                    ),
                    this
                )
            }
        }.let { /* Require exhaustive when */ }
    }

    override fun onPlaybackStateChanged(newState: MediaPlayerState) {
        val deadSubscribers = mutableListOf<ClientBidirectionalMessenger>()

        subscribers.forEach { subscriber ->
            try {
                subscriber.send(
                    message = ServiceClientMessage.UpdateState(newState),
                    respondTo = messenger
                )
            } catch (e: DeadObjectException) {
                deadSubscribers += subscriber
            }
        }

        subscribers.removeAll(deadSubscribers)
    }

}
