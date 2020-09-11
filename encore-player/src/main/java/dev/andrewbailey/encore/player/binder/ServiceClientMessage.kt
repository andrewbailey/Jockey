package dev.andrewbailey.encore.player.binder

import android.os.Parcelable
import android.support.v4.media.session.MediaSessionCompat
import dev.andrewbailey.encore.model.MediaItem
import dev.andrewbailey.encore.player.state.MediaPlayerState
import kotlinx.android.parcel.Parcelize

internal sealed class ServiceClientMessage<out M : MediaItem> : Parcelable {

    @Parcelize
    data class Initialize<M : MediaItem>(
        val firstState: MediaPlayerState<M>,
        val mediaSessionToken: MediaSessionCompat.Token
    ) : ServiceClientMessage<M>()

    @Parcelize
    data class UpdateState<M : MediaItem>(
        val newState: MediaPlayerState<M>
    ) : ServiceClientMessage<M>()

}
