package dev.andrewbailey.encore.player.state

import android.os.Parcelable
import dev.andrewbailey.encore.model.MediaObject
import kotlinx.parcelize.Parcelize

public sealed class TransportState<out M : MediaObject> : Parcelable {

    public abstract val repeatMode: RepeatMode
    public abstract val shuffleMode: ShuffleMode
    public abstract val playbackSpeed: Float

    @Parcelize
    public data class Active<out M : MediaObject>(
        val status: PlaybackStatus,
        val seekPosition: SeekPosition,
        val queue: QueueState<M>,
        override val repeatMode: RepeatMode,
        override val playbackSpeed: Float
    ) : TransportState<M>() {

        init {
            require(playbackSpeed > 0) {
                "Playback speed must be positive."
            }
        }

        override val shuffleMode: ShuffleMode
            get() = if (queue is QueueState.Linear) {
                ShuffleMode.ShuffleDisabled
            } else {
                ShuffleMode.ShuffleEnabled
            }

    }

    @Parcelize
    public data class Idle(
        override val repeatMode: RepeatMode,
        override val shuffleMode: ShuffleMode,
        override val playbackSpeed: Float
    ) : TransportState<Nothing>() {

        init {
            require(playbackSpeed > 0) {
                "Playback speed must be positive."
            }
        }

    }

}
