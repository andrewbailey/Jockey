package dev.andrewbailey.encore.player.state

import android.graphics.Bitmap
import android.os.Parcelable
import dev.andrewbailey.encore.model.MediaObject
import kotlinx.parcelize.Parcelize

public sealed class MediaPlayerState<out M : MediaObject> : Parcelable {

    public abstract val transportState: TransportState<M>

    @Parcelize
    public class Prepared<M : MediaObject> internal constructor(
        override val transportState: TransportState.Active<M>,
        public val artwork: Bitmap?,
        public val durationMs: Long?,
        public val bufferingState: BufferingState
    ) : MediaPlayerState<M>()

    @Parcelize
    public class Ready internal constructor(
        override val transportState: TransportState.Idle
    ) : MediaPlayerState<Nothing>()

}
