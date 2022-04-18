package dev.andrewbailey.encore.player.state

import android.os.Parcelable
import android.os.SystemClock
import dev.andrewbailey.annotations.compose.ComposeStableClass
import kotlin.math.min
import kotlin.math.roundToLong
import kotlinx.parcelize.Parcelize

public sealed class SeekPosition : Parcelable {

    public abstract val seekPositionMillis: Long

    public operator fun compareTo(other: SeekPosition): Int {
        return seekPositionMillis.compareTo(other.seekPositionMillis)
    }

    @Parcelize
    internal data class ComputedSeekPosition(
        val originalSeekPositionMillis: Long,
        val maxSeekPositionMillis: Long,
        val creationTimeMillis: Long = SystemClock.elapsedRealtime(),
        val playbackSpeed: Float
    ) : SeekPosition() {

        override val seekPositionMillis: Long
            get() {
                val dT = SystemClock.elapsedRealtime() - creationTimeMillis
                val computedTime = (originalSeekPositionMillis + dT * playbackSpeed).roundToLong()
                return min(computedTime, maxSeekPositionMillis)
            }
    }

    @ComposeStableClass
    @Parcelize
    public data class AbsoluteSeekPosition(
        override val seekPositionMillis: Long
    ) : SeekPosition()

}
