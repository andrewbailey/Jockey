package dev.andrewbailey.encore.player.playback

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.metadata.flac.PictureFrame
import com.google.android.exoplayer2.metadata.id3.ApicFrame
import dev.andrewbailey.encore.player.state.PlaybackState
import dev.andrewbailey.encore.player.util.getEntries
import dev.andrewbailey.encore.player.util.getFormats
import dev.andrewbailey.encore.player.util.toList

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

    fun getArtwork(): Bitmap? {
        return exoPlayer.currentTrackSelections.toList()
            .flatMap { it.getFormats() }
            .mapNotNull { it.metadata }
            .flatMap { it.getEntries() }
            .asSequence()
            .mapNotNull { metadataEntry ->
                when (metadataEntry) {
                    is ApicFrame -> {
                        metadataEntry.pictureData
                    }
                    is PictureFrame -> {
                        metadataEntry.pictureData
                    }
                    else -> null
                }
            }
            .firstOrNull()
            ?.let { BitmapFactory.decodeByteArray(it, 0, it.size) }
    }

    fun release() {
        extensions.forEach { it.release() }
        observers.forEach { it.onRelease() }
        exoPlayer.release()
    }

}
