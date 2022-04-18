package dev.andrewbailey.encore.player.state

import android.os.Parcelable
import dev.andrewbailey.annotations.compose.ComposeStableClass
import dev.andrewbailey.encore.model.MediaObject
import kotlinx.parcelize.Parcelize

@ComposeStableClass
public sealed class MediaPlaybackState<out M : MediaObject> : Parcelable {

    public abstract val repeatMode: RepeatMode
    public abstract val shuffleMode: ShuffleMode
    public abstract val playbackSpeed: Float

    @ComposeStableClass
    @Parcelize
    public data class Populated<out M : MediaObject>(
        val status: PlaybackStatus,
        val seekPosition: SeekPosition,
        val queue: QueueState<M>,
        override val repeatMode: RepeatMode,
        override val playbackSpeed: Float
    ) : MediaPlaybackState<M>() {

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

    @ComposeStableClass
    @Parcelize
    public data class Empty(
        override val repeatMode: RepeatMode,
        override val shuffleMode: ShuffleMode,
        override val playbackSpeed: Float
    ) : MediaPlaybackState<Nothing>() {

        init {
            require(playbackSpeed > 0) {
                "Playback speed must be positive."
            }
        }

    }

}
