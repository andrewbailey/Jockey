package dev.andrewbailey.encore.player.state

import android.os.Parcelable
import android.os.SystemClock
import kotlinx.android.parcel.Parcelize

sealed class SeekPosition : Parcelable {

    abstract val seekPositionMillis: Long

    operator fun compareTo(other: SeekPosition): Int {
        return seekPositionMillis.compareTo(other.seekPositionMillis)
    }

    @Parcelize
    internal data class ComputedSeekPosition(
        val originalSeekPositionMillis: Long,
        val creationTimeMillis: Long = SystemClock.elapsedRealtime()
    ) : SeekPosition() {

        override val seekPositionMillis: Long
            get() {
                val dT = SystemClock.elapsedRealtime() - creationTimeMillis
                return originalSeekPositionMillis + dT
            }
    }

    @Parcelize
    data class AbsoluteSeekPosition(
        override val seekPositionMillis: Long
    ) : SeekPosition()

}
