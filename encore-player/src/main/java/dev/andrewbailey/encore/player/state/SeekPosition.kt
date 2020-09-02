package dev.andrewbailey.encore.player.state

import android.os.Parcelable
import android.os.SystemClock
import kotlin.math.min
import kotlinx.android.parcel.Parcelize

public sealed class SeekPosition : Parcelable {

    public abstract val seekPositionMillis: Long

    public operator fun compareTo(other: SeekPosition): Int {
        return seekPositionMillis.compareTo(other.seekPositionMillis)
    }

    @Parcelize
    internal data class ComputedSeekPosition(
        val originalSeekPositionMillis: Long,
        val maxSeekPositionMillis: Long,
        val creationTimeMillis: Long = SystemClock.elapsedRealtime()
    ) : SeekPosition() {

        override val seekPositionMillis: Long
            get() {
                val dT = SystemClock.elapsedRealtime() - creationTimeMillis
                val computedTime = originalSeekPositionMillis + dT
                return min(computedTime, maxSeekPositionMillis)
            }
    }

    @Parcelize
    public data class AbsoluteSeekPosition(
        override val seekPositionMillis: Long
    ) : SeekPosition()

}
