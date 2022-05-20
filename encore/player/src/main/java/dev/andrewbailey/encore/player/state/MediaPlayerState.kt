package dev.andrewbailey.encore.player.state

import android.graphics.Bitmap
import android.os.Parcelable
import dev.andrewbailey.encore.model.MediaObject
import kotlinx.parcelize.Parcelize

public sealed class MediaPlayerState<out M : MediaObject> : Parcelable {

    public abstract val mediaPlaybackState: MediaPlaybackState<M>?

    @Parcelize
    public object Initializing : MediaPlayerState<Nothing>() {
        override val mediaPlaybackState: MediaPlaybackState<Nothing>?
            get() = null
    }

    public sealed class Initialized<out M : MediaObject> : MediaPlayerState<M>() {
        public abstract override val mediaPlaybackState: MediaPlaybackState<M>
    }

    @Parcelize
    public class Prepared<M : MediaObject> internal constructor(
        override val mediaPlaybackState: MediaPlaybackState.Populated<M>,
        public val artwork: Bitmap?,
        public val durationMs: Long?,
        public val bufferingState: BufferingState
    ) : MediaPlayerState.Initialized<M>() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true

            return other is Prepared<*> &&
                mediaPlaybackState == other.mediaPlaybackState &&
                artwork == other.artwork &&
                durationMs == other.durationMs &&
                bufferingState == other.bufferingState
        }

        override fun hashCode(): Int {
            var result = mediaPlaybackState.hashCode()
            result = 31 * result + artwork.hashCode()
            result = 31 * result + durationMs.hashCode()
            result = 31 * result + bufferingState.hashCode()
            return result
        }

        override fun toString(): String {
            return "MediaPlayerState.Prepared(" +
                "transportState=$mediaPlaybackState, " +
                "artwork=$artwork, " +
                "durationMs=$durationMs, " +
                "bufferingState=$bufferingState)"
        }
    }

    @Parcelize
    public class Ready internal constructor(
        override val mediaPlaybackState: MediaPlaybackState.Empty
    ) : MediaPlayerState.Initialized<Nothing>() {
        override fun equals(other: Any?): Boolean {
            return (this === other) ||
                (other is Ready && mediaPlaybackState == other.mediaPlaybackState)
        }

        override fun hashCode(): Int {
            return 31 * mediaPlaybackState.hashCode()
        }

        override fun toString(): String {
            return "MediaPlayerState.Ready(transportState=$mediaPlaybackState)"
        }
    }

}
