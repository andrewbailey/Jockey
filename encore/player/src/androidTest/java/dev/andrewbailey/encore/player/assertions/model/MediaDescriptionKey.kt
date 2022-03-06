package dev.andrewbailey.encore.player.assertions.model

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.support.v4.media.MediaDescriptionCompat

sealed class MediaDescriptionKey<out T>(
    val name: String,
    val lookup: MediaDescriptionCompat.() -> T,
) {

    override fun toString(): String {
        return name
    }

    object MediaId : MediaDescriptionKey<String?>(
        name = "MediaId",
        lookup = MediaDescriptionCompat::getMediaId
    )

    object Title : MediaDescriptionKey<CharSequence?>(
        name = "Title",
        lookup = MediaDescriptionCompat::getTitle
    )

    object Subtitle : MediaDescriptionKey<CharSequence?>(
        name = "Subtitle",
        lookup = MediaDescriptionCompat::getSubtitle
    )

    object Description : MediaDescriptionKey<CharSequence?>(
        name = "Description",
        lookup = MediaDescriptionCompat::getDescription
    )

    object IconBitmap : MediaDescriptionKey<Bitmap?>(
        name = "IconBitmap",
        lookup = MediaDescriptionCompat::getIconBitmap
    )

    object IconUri : MediaDescriptionKey<Uri?>(
        name = "IconUri",
        lookup = MediaDescriptionCompat::getIconUri
    )

    object Extras : MediaDescriptionKey<Bundle?>(
        name = "Extras",
        lookup = MediaDescriptionCompat::getExtras
    )

    object MediaUri : MediaDescriptionKey<Uri?>(
        name = "MediaUri",
        lookup = MediaDescriptionCompat::getMediaUri
    )

    companion object {
        fun values() = listOf(
            MediaId, Title, Subtitle, Description, IconBitmap, IconUri, Extras, MediaUri
        )
    }

}

fun MediaDescriptionCompat.asMap(
    includeNullValues: Boolean = true
): Map<MediaDescriptionKey<Any?>, Any?> =
    MediaDescriptionKey.values().associateWith { this[it] }
        .filterValues { includeNullValues || it != null }

@Suppress("UNCHECKED_CAST")
operator fun <T> MediaDescriptionCompat.get(item: MediaDescriptionKey<T>): T {
    return item.lookup(this)
}
