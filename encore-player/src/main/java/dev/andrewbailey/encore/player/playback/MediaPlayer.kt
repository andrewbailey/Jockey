package dev.andrewbailey.encore.player.playback

import android.content.Context
import com.google.android.exoplayer2.ExoPlayerFactory
import dev.andrewbailey.encore.player.state.PlaybackState

internal class MediaPlayer(
    context: Context,
    private val extensions: List<PlaybackExtension>,
    private val observers: List<PlaybackObserver>
) {

    private val exoPlayer = ExoPlayerFactory.newSimpleInstance(context)
    private val queue = MediaQueue()

    init {
        extensions.forEach { it.initialize(this) }
    }

    fun getState(): PlaybackState {
        TODO()
    }

    fun setState(state: PlaybackState) {
        TODO()
    }

    fun release() {
        extensions.forEach { it.release() }
        observers.forEach { it.onRelease() }
        TODO()
    }

}
