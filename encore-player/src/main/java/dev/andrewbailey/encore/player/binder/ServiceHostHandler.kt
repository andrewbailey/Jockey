package dev.andrewbailey.encore.player.binder

import android.os.DeadObjectException
import android.support.v4.media.session.MediaSessionCompat
import dev.andrewbailey.encore.model.MediaObject
import dev.andrewbailey.encore.player.playback.PlaybackObserver
import dev.andrewbailey.encore.player.state.MediaPlayerState
import dev.andrewbailey.encore.player.state.TransportState
import dev.andrewbailey.encore.player.state.diff.MediaPlayerStateDiffer
import dev.andrewbailey.ipc.bidirectionalMessenger

internal class ServiceHostHandler<M : MediaObject>(
    private val getState: () -> MediaPlayerState<M>,
    private val getMediaSession: () -> MediaSessionCompat,
    private val onSetState: (TransportState<M>) -> Unit
) : PlaybackObserver<M> {

    private val stateDiffer = MediaPlayerStateDiffer<M>()

    private val subscribers = mutableListOf<ClientBidirectionalMessenger<M>>()
    private val lastDispatchedStates =
        mutableMapOf<ClientBidirectionalMessenger<M>, MediaPlayerState<M>?>()

    val messenger = ServiceBidirectionalMessenger<M>(
        bidirectionalMessenger { data, replyTo ->
            when (data) {
                is ServiceHostMessage.SetState -> {
                    onSetState(data.newState)
                }
                ServiceHostMessage.Initialize -> {
                    val subscriber = ClientBidirectionalMessenger(replyTo)
                    val state = getState()
                    subscribers += subscriber
                    lastDispatchedStates += subscriber to state
                    replyTo.send(
                        ServiceClientMessage.Initialize(
                            firstState = state,
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
                val lastDispatchedState = lastDispatchedStates[subscriber]
                if (lastDispatchedState !is MediaPlayerState.Prepared ||
                    newState !is MediaPlayerState.Prepared
                ) {
                    subscriber.send(
                        message = ServiceClientMessage.UpdateState(newState),
                        respondTo = messenger
                    )
                } else {
                    subscriber.send(
                        message = ServiceClientMessage.UpdateStateFromDiff(
                            stateDiff = stateDiffer.generateDiff(
                                toState = newState,
                                fromState = lastDispatchedState
                            )
                        ),
                        respondTo = messenger
                    )
                }
                this.lastDispatchedStates[subscriber] = newState
            } catch (e: DeadObjectException) {
                deadSubscribers += subscriber
            }
        }

        subscribers -= deadSubscribers
        lastDispatchedStates -= deadSubscribers
    }

}
