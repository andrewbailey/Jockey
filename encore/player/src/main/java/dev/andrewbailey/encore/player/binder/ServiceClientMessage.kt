package dev.andrewbailey.encore.player.binder

import android.os.Parcelable
import android.support.v4.media.session.MediaSessionCompat
import dev.andrewbailey.encore.model.MediaObject
import dev.andrewbailey.encore.player.state.MediaPlayerState
import dev.andrewbailey.encore.player.state.diff.MediaPlayerStateDiff
import kotlinx.parcelize.Parcelize

internal sealed class ServiceClientMessage<out M : MediaObject> : Parcelable {

    @Parcelize
    data class Initialize<M : MediaObject>(
        val firstState: MediaPlayerState<M>,
        val mediaSessionToken: MediaSessionCompat.Token
    ) : ServiceClientMessage<M>()

    @Parcelize
    data class UpdateStateFromDiff<M : MediaObject>(
        val stateDiff: MediaPlayerStateDiff<M>
    ) : ServiceClientMessage<M>()

    @Parcelize
    data class UpdateState<M : MediaObject>(
        val newState: MediaPlayerState<M>
    ) : ServiceClientMessage<M>()

}
