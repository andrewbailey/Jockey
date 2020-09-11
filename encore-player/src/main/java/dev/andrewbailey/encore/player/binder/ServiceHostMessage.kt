package dev.andrewbailey.encore.player.binder

import android.os.Parcelable
import dev.andrewbailey.encore.model.MediaItem
import dev.andrewbailey.encore.player.state.TransportState
import kotlinx.android.parcel.Parcelize

internal sealed class ServiceHostMessage<out M : MediaItem> : Parcelable {

    @Parcelize
    data class SetState<M : MediaItem>(
        val newState: TransportState<M>
    ) : ServiceHostMessage<M>()

    @Parcelize
    object Initialize : ServiceHostMessage<Nothing>()

}
