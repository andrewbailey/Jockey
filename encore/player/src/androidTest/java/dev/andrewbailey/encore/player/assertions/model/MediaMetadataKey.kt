package dev.andrewbailey.encore.player.assertions.model

import android.graphics.Bitmap
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.RatingCompat

sealed class MediaMetadataKey<out T>(
    val key: String,
    val type: Class<out T>,
    val defaultValue: T? = when (type) {
        Long::class.java -> 0L as T?
        String::class.java -> null
        RatingCompat::class.java -> null
        Bitmap::class.java -> null
        else -> throw IllegalArgumentException("Unsupported type ${type.simpleName}")
    }
) {

    override fun toString(): String {
        return key
    }

    object Title : MediaMetadataKey<String>(
        key = MediaMetadataCompat.METADATA_KEY_TITLE,
        type = String::class.java
    )

    object Artist : MediaMetadataKey<String>(
        key = MediaMetadataCompat.METADATA_KEY_ARTIST,
        type = String::class.java
    )

    object Duration : MediaMetadataKey<Long>(
        key = MediaMetadataCompat.METADATA_KEY_DURATION,
        type = Long::class.java
    )

    object Album : MediaMetadataKey<String>(
        key = MediaMetadataCompat.METADATA_KEY_ALBUM,
        type = String::class.java
    )

    object Author : MediaMetadataKey<String>(
        key = MediaMetadataCompat.METADATA_KEY_AUTHOR,
        type = String::class.java
    )

    object Writer : MediaMetadataKey<String>(
        key = MediaMetadataCompat.METADATA_KEY_WRITER,
        type = String::class.java
    )

    object Composer : MediaMetadataKey<String>(
        key = MediaMetadataCompat.METADATA_KEY_COMPOSER,
        type = String::class.java
    )

    object Compilation : MediaMetadataKey<String>(
        key = MediaMetadataCompat.METADATA_KEY_COMPILATION,
        type = String::class.java
    )

    object Date : MediaMetadataKey<String>(
        key = MediaMetadataCompat.METADATA_KEY_DATE,
        type = String::class.java
    )

    object Year : MediaMetadataKey<Long>(
        key = MediaMetadataCompat.METADATA_KEY_YEAR,
        type = Long::class.java
    )

    object Genre : MediaMetadataKey<String>(
        key = MediaMetadataCompat.METADATA_KEY_GENRE,
        type = String::class.java
    )

    object TrackNumber : MediaMetadataKey<Long>(
        key = MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER,
        type = Long::class.java
    )

    object NumTracks : MediaMetadataKey<Long>(
        key = MediaMetadataCompat.METADATA_KEY_NUM_TRACKS,
        type = Long::class.java
    )

    object DiscNumber : MediaMetadataKey<Long>(
        key = MediaMetadataCompat.METADATA_KEY_DISC_NUMBER,
        type = Long::class.java
    )

    object AlbumArtist : MediaMetadataKey<String>(
        key = MediaMetadataCompat.METADATA_KEY_ALBUM_ARTIST,
        type = String::class.java
    )

    object Art : MediaMetadataKey<Bitmap>(
        key = MediaMetadataCompat.METADATA_KEY_ART,
        type = Bitmap::class.java
    )

    object ArtUri : MediaMetadataKey<String>(
        key = MediaMetadataCompat.METADATA_KEY_ART_URI,
        type = String::class.java
    )

    object AlbumArt : MediaMetadataKey<Bitmap>(
        key = MediaMetadataCompat.METADATA_KEY_ALBUM_ART,
        type = Bitmap::class.java
    )

    object AlbumArtUri : MediaMetadataKey<String>(
        key = MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI,
        type = String::class.java
    )

    object UserRating : MediaMetadataKey<RatingCompat>(
        key = MediaMetadataCompat.METADATA_KEY_USER_RATING,
        type = RatingCompat::class.java
    )

    object Rating : MediaMetadataKey<RatingCompat>(
        key = MediaMetadataCompat.METADATA_KEY_RATING,
        type = RatingCompat::class.java
    )

    object DisplayTitle : MediaMetadataKey<String>(
        key = MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE,
        type = String::class.java
    )

    object DisplaySubtitle : MediaMetadataKey<String>(
        key = MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE,
        type = String::class.java
    )

    object DisplayDescription : MediaMetadataKey<String>(
        key = MediaMetadataCompat.METADATA_KEY_DISPLAY_DESCRIPTION,
        type = String::class.java
    )

    object DisplayIcon : MediaMetadataKey<Bitmap>(
        key = MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON,
        type = Bitmap::class.java
    )

    object DisplayIconUri : MediaMetadataKey<String>(
        key = MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI,
        type = String::class.java
    )

    object MediaId : MediaMetadataKey<String>(
        key = MediaMetadataCompat.METADATA_KEY_MEDIA_ID,
        type = String::class.java
    )

    object MediaUri : MediaMetadataKey<String>(
        key = MediaMetadataCompat.METADATA_KEY_MEDIA_URI,
        type = String::class.java
    )

    object BtFolderType : MediaMetadataKey<String>(
        key = MediaMetadataCompat.METADATA_KEY_BT_FOLDER_TYPE,
        type = String::class.java
    )

    object Advertisement : MediaMetadataKey<Long>(
        key = MediaMetadataCompat.METADATA_KEY_ADVERTISEMENT,
        type = Long::class.java
    )

    object DownloadStatus : MediaMetadataKey<Long>(
        key = MediaMetadataCompat.METADATA_KEY_DOWNLOAD_STATUS,
        type = Long::class.java
    )

    companion object {
        fun values() = listOf(
            Title, Artist, Duration, Album, Author, Writer, Composer, Compilation, Date, Year,
            Genre, TrackNumber, NumTracks, DiscNumber, AlbumArtist, Art, ArtUri, AlbumArt,
            AlbumArtUri, UserRating, Rating, DisplayTitle, DisplaySubtitle, DisplayDescription,
            DisplayIcon, DisplayIconUri, MediaId, MediaUri, BtFolderType, Advertisement,
            DownloadStatus
        )
    }

}

fun MediaMetadataCompat.asMap(): Map<MediaMetadataKey<Any>, Any?> = MediaMetadataKey.values()
    .associateWith { this[it] }

@Suppress("UNCHECKED_CAST")
operator fun <T> MediaMetadataCompat.get(item: MediaMetadataKey<T>): T? {
    return when (item.type) {
        Long::class.java -> getLong(item.key) as T?
        String::class.java -> getString(item.key) as T?
        RatingCompat::class.java -> getRating(item.key) as T?
        Bitmap::class.java -> getBitmap(item.key) as T?
        else -> throw IllegalArgumentException("Unsupported type ${item.type.simpleName}")
    }
}
