package dev.andrewbailey.encore.player.playback

import android.content.Context
import com.google.android.exoplayer2.ExoPlayerFactory
import dev.andrewbailey.encore.player.state.PlaybackState

internal class MediaPlayer(
    context: Context
) {

    private val exoPlayer = ExoPlayerFactory.newSimpleInstance(context)
    private val queue = MediaQueue()

    init {
    }

    fun getState(): PlaybackState {
        TODO()
    }

    fun setState(state: PlaybackState) {
        TODO()
    }

    fun release() {
        TODO()
    }

}
