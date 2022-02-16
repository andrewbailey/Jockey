package dev.andrewbailey.encore.mediaresumption.impl.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import dev.andrewbailey.encore.player.state.RepeatMode
import dev.andrewbailey.encore.player.state.ShuffleMode

@Entity(tableName = "playback_state")
internal data class PersistedPlaybackState(
    val seekPositionMs: Long,
    val queueIndex: Int,
    val shuffleMode: ShuffleMode,
    val repeatMode: RepeatMode,
    val playbackSpeed: Float
) {
    @PrimaryKey
    var id: Int = 0
        @Deprecated("ID should never be set. This is only present to satisfy the Room compiler.")
        internal set
}
