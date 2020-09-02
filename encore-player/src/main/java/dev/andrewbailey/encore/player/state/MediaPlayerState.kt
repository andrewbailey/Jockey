package dev.andrewbailey.encore.player.state

import android.graphics.Bitmap
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

public sealed class MediaPlayerState : Parcelable {

    public abstract val transportState: TransportState

    @Parcelize
    public class Prepared internal constructor(
        override val transportState: TransportState.Active,
        public val artwork: Bitmap?,
        public val durationMs: Long?,
        public val bufferingState: BufferingState
    ) : MediaPlayerState()

    @Parcelize
    public class Ready internal constructor(
        override val transportState: TransportState.Idle
    ) : MediaPlayerState()

}
