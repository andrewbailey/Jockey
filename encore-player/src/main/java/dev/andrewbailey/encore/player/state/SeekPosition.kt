package dev.andrewbailey.encore.player.state

import android.os.SystemClock

sealed class SeekPosition {

    abstract val seekPositionMillis: Long

    operator fun compareTo(other: SeekPosition): Int {
        return seekPositionMillis.compareTo(other.seekPositionMillis)
    }

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

    data class AbsoluteSeekPosition(
        override val seekPositionMillis: Long
    ) : SeekPosition()

}
