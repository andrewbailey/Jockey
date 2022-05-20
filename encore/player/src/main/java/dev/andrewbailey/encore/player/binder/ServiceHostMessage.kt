package dev.andrewbailey.encore.player.binder

import android.os.Parcelable
import dev.andrewbailey.encore.model.MediaObject
import dev.andrewbailey.encore.player.state.MediaPlaybackState
import kotlinx.parcelize.Parcelize

internal sealed class ServiceHostMessage<out M : MediaObject> : Parcelable {

    @Parcelize
    data class SetState<M : MediaObject>(
        val newState: MediaPlaybackState<M>
    ) : ServiceHostMessage<M>()

    @Parcelize
    object Initialize : ServiceHostMessage<Nothing>()

}
