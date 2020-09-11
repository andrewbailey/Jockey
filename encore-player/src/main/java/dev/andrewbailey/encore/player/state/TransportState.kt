package dev.andrewbailey.encore.player.state

import android.os.Parcelable
import dev.andrewbailey.encore.model.MediaItem
import kotlinx.android.parcel.Parcelize

public sealed class TransportState<out M : MediaItem> : Parcelable {

    public abstract val repeatMode: RepeatMode
    public abstract val shuffleMode: ShuffleMode

    @Parcelize
    public data class Active<out M : MediaItem>(
        val status: PlaybackState,
        val seekPosition: SeekPosition,
        val queue: QueueState<M>,
        override val repeatMode: RepeatMode
    ) : TransportState<M>() {

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
    ) : TransportState<Nothing>()

}
