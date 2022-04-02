package dev.andrewbailey.encore.player.assertions.mediasession

import android.net.Uri
import android.support.v4.media.MediaBrowserCompat
import com.google.common.truth.Correspondence
import dev.andrewbailey.encore.model.MediaObject

val MediaBrowserItemCorrespondence = Correspondence.from(
    { first: MediaBrowserCompat.MediaItem?, second: MediaObject? ->
        when {
            first == null && second == null -> true
            first != null && second == null -> false
            first == null && second != null -> false
            else -> {
                first!!.description.let { description ->
                    val metadata = second!!.toMediaMetadata()

                    description.title == metadata.title &&
                        description.subtitle == metadata.subtitle &&
                        description.description == metadata.description &&
                        description.mediaUri == Uri.parse(second.playbackUri)
                } && !first.isBrowsable && first.isPlayable
            }
        }
    },
    "is equivalent to"
)
