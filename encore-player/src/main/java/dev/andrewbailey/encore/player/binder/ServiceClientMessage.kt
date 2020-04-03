package dev.andrewbailey.encore.player.binder

import android.os.Parcelable
import android.support.v4.media.session.MediaSessionCompat
import dev.andrewbailey.encore.player.state.MediaPlayerState
import kotlinx.android.parcel.Parcelize

internal sealed class ServiceClientMessage : Parcelable {

    @Parcelize
    data class Initialize(
        val firstState: MediaPlayerState,
        val mediaSessionToken: MediaSessionCompat.Token
    ) : ServiceClientMessage()

    @Parcelize
    data class UpdateState(
        val newState: MediaPlayerState
    ) : ServiceClientMessage()

}
