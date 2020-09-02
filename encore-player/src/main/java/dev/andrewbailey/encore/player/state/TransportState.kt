package dev.andrewbailey.encore.player.state

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

public sealed class TransportState : Parcelable {

    public abstract val repeatMode: RepeatMode
    public abstract val shuffleMode: ShuffleMode

    @Parcelize
    public data class Active(
        val status: PlaybackState,
        val seekPosition: SeekPosition,
        val queue: QueueState,
        override val repeatMode: RepeatMode
    ) : TransportState() {

        override val shuffleMode: ShuffleMode
            get() = if (queue is QueueState.Linear) {
                ShuffleMode.LINEAR
            } else {
                ShuffleMode.SHUFFLED
            }

    }

    @Parcelize
    public data class Idle(
        override val repeatMode: RepeatMode,
        override val shuffleMode: ShuffleMode
    ) : TransportState()

}
