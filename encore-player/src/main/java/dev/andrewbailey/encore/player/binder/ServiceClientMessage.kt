package dev.andrewbailey.encore.player.binder

import android.os.Parcelable
import android.support.v4.media.session.MediaSessionCompat
import kotlinx.android.parcel.Parcelize

internal sealed class ServiceClientMessage : Parcelable {

    @Parcelize
    data class Initialize(
        val mediaSessionToken: MediaSessionCompat.Token
    ) : ServiceClientMessage()
}
