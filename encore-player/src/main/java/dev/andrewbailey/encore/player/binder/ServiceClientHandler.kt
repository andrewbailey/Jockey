package dev.andrewbailey.encore.player.binder

import android.content.Context
import android.support.v4.media.session.MediaControllerCompat
import dev.andrewbailey.encore.model.MediaObject
import dev.andrewbailey.encore.player.state.MediaPlayerState
import dev.andrewbailey.encore.player.state.diff.MediaPlayerStateDiff
import dev.andrewbailey.ipc.bidirectionalMessenger

internal class ServiceClientHandler<M : MediaObject>(
    private val context: Context,
    private val onSetMediaController: (MediaControllerCompat) -> Unit,
    private val onSetMediaPlayerState: (MediaPlayerState<M>) -> Unit,
    private val onApplyMediaStateDiff: (MediaPlayerStateDiff<M>) -> Unit
) {

    val messenger = ClientBidirectionalMessenger<M>(
        bidirectionalMessenger { data, replyTo ->
            when (data) {
                is ServiceClientMessage.Initialize<M> -> {
                    onSetMediaController(MediaControllerCompat(context, data.mediaSessionToken))
                    onSetMediaPlayerState(data.firstState)
                }
                is ServiceClientMessage.UpdateStateFromDiff<M> -> {
                    onApplyMediaStateDiff(data.stateDiff)
                }
                is ServiceClientMessage.UpdateState<M> -> {
                    onSetMediaPlayerState(data.newState)
                }
            }.let { /* Require exhaustive let */ }
        }
    )

}
