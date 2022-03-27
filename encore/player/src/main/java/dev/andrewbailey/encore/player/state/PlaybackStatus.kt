package dev.andrewbailey.encore.player.state

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

public sealed class PlaybackStatus : Parcelable {

    @Parcelize
    public object Playing : PlaybackStatus()

    @Parcelize
    public class Paused internal constructor(
        public val reachedEndOfQueue: Boolean
    ) : PlaybackStatus() {

        public constructor() : this(
            reachedEndOfQueue = false
        )

        override fun equals(other: Any?): Boolean {
            if (this === other) return true

            return other is Paused &&
                other.reachedEndOfQueue == this.reachedEndOfQueue
        }

        override fun hashCode(): Int {
            return reachedEndOfQueue.hashCode()
        }

        override fun toString(): String {
            return "Paused(reachedEndOfQueue=$reachedEndOfQueue)"
        }

    }

}
