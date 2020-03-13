package dev.andrewbailey.encore.player.binder

import android.os.Parcelable
import dev.andrewbailey.encore.player.state.TransportState
import kotlinx.android.parcel.Parcelize

internal sealed class ServiceHostMessage : Parcelable {

    @Parcelize
    data class SetState(
        val newState: TransportState
    ) : ServiceHostMessage()

}
