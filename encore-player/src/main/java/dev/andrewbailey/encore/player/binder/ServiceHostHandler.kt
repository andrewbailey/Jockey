package dev.andrewbailey.encore.player.binder

import android.os.DeadObjectException
import android.support.v4.media.session.MediaSessionCompat
import dev.andrewbailey.encore.model.MediaObject
import dev.andrewbailey.encore.player.playback.PlaybackObserver
import dev.andrewbailey.encore.player.state.MediaPlayerState
import dev.andrewbailey.encore.player.state.TransportState
import dev.andrewbailey.ipc.bidirectionalMessenger

internal class ServiceHostHandler<M : MediaObject>(
    private val getState: () -> MediaPlayerState<M>,
    private val getMediaSession: () -> MediaSessionCompat,
    private val onSetState: (TransportState<M>) -> Unit
) : PlaybackObserver<M> {

    private val subscribers = mutableListOf<ClientBidirectionalMessenger<M>>()

    val messenger = ServiceBidirectionalMessenger<M>(
        bidirectionalMessenger { data, replyTo ->
            when (data) {
                is ServiceHostMessage.SetState -> {
                    onSetState(data.newState)
                }
                ServiceHostMessage.Initialize -> {
                    subscribers += ClientBidirectionalMessenger(replyTo)
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
    )

    override fun onPlaybackStateChanged(newState: MediaPlayerState<M>) {
        val deadSubscribers = mutableListOf<ClientBidirectionalMessenger<M>>()

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
