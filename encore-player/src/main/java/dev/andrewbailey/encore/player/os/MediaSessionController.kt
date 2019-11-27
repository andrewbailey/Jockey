package dev.andrewbailey.encore.player.os

import android.content.Context
import android.support.v4.media.session.MediaSessionCompat
import dev.andrewbailey.encore.player.playback.PlaybackExtension

internal class MediaSessionController(
    context: Context,
    tag: String
) : PlaybackExtension() {

    val mediaSession = MediaSessionCompat(context, tag)

    override fun onRelease() {
        mediaSession.release()
    }

}
