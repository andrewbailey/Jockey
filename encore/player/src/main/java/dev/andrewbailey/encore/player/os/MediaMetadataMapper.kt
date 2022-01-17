package dev.andrewbailey.encore.player.os

import android.support.v4.media.MediaDescriptionCompat.STATUS_DOWNLOADED
import android.support.v4.media.MediaDescriptionCompat.STATUS_DOWNLOADING
import android.support.v4.media.MediaDescriptionCompat.STATUS_NOT_DOWNLOADED
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_ADVERTISEMENT
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_ALBUM
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_ALBUM_ART
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_ALBUM_ARTIST
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_ART
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_ARTIST
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_ART_URI
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_AUTHOR
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_COMPOSER
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_DATE
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_DISC_NUMBER
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_DISPLAY_DESCRIPTION
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_DOWNLOAD_STATUS
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_DURATION
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_GENRE
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_NUM_TRACKS
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_RATING
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_TITLE
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_USER_RATING
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_WRITER
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_YEAR
import android.support.v4.media.RatingCompat
import android.support.v4.media.RatingCompat.RATING_3_STARS
import android.support.v4.media.RatingCompat.RATING_4_STARS
import android.support.v4.media.RatingCompat.RATING_5_STARS
import android.support.v4.media.RatingCompat.RATING_HEART
import android.support.v4.media.RatingCompat.RATING_PERCENTAGE
import android.support.v4.media.RatingCompat.RATING_THUMB_UP_DOWN
import android.support.v4.media.RatingCompat.newHeartRating
import android.support.v4.media.RatingCompat.newPercentageRating
import android.support.v4.media.RatingCompat.newStarRating
import android.support.v4.media.RatingCompat.newThumbRating
import android.support.v4.media.RatingCompat.newUnratedRating
import dev.andrewbailey.encore.model.MediaDownloadStatus
import dev.andrewbailey.encore.model.MediaRating
import dev.andrewbailey.encore.model.RatingScale
import dev.andrewbailey.encore.model.RatingValue.HeartRatingValue.Hearted
import dev.andrewbailey.encore.model.RatingValue.PercentageRatingValue
import dev.andrewbailey.encore.model.RatingValue.StarRatingValue
import dev.andrewbailey.encore.model.RatingValue.ThumbRatingValue.ThumbsUp
import dev.andrewbailey.encore.player.state.MediaPlayerState

internal class MediaMetadataMapper {

    fun toMediaMetadataCompat(
        playbackState: MediaPlayerState.Prepared<*>
    ): MediaMetadataCompat = MediaMetadataCompat.Builder().apply {
        val metadata = playbackState.transportState.queue.nowPlaying.mediaItem.toMediaMetadata()
        val isAdvertisement = false

        putString(METADATA_KEY_TITLE, metadata.title)
        putLong(METADATA_KEY_DURATION, playbackState.durationMs ?: -1)

        playbackState.artwork?.let {
            putBitmap(METADATA_KEY_ART, it)
            putBitmap(METADATA_KEY_ALBUM_ART, it)
        }

        metadata.artistName?.let { putString(METADATA_KEY_ARTIST, it) }
        metadata.albumName?.let { putString(METADATA_KEY_ALBUM, it) }
        metadata.authorName?.let { putString(METADATA_KEY_AUTHOR, it) }
        metadata.writerName?.let { putString(METADATA_KEY_WRITER, it) }
        metadata.composerName?.let { putString(METADATA_KEY_COMPOSER, it) }
        metadata.publishedAt?.toString()?.let { putString(METADATA_KEY_DATE, it) }
        metadata.genreName?.let { putString(METADATA_KEY_GENRE, it) }
        metadata.albumArtistName?.let { putString(METADATA_KEY_ALBUM_ARTIST, it) }
        metadata.artworkUri?.let {
            putString(METADATA_KEY_ART_URI, it)
            putString(METADATA_KEY_ALBUM_ART_URI, it)
        }
        metadata.subtitle?.let { putString(METADATA_KEY_DISPLAY_SUBTITLE, it) }
        metadata.description?.let { putString(METADATA_KEY_DISPLAY_DESCRIPTION, it) }
        metadata.trackNumber?.let { putLong(METADATA_KEY_TRACK_NUMBER, it.toLong()) }
        metadata.numberOfTracks?.let { putLong(METADATA_KEY_NUM_TRACKS, it.toLong()) }
        metadata.discNumber?.let { putLong(METADATA_KEY_DISC_NUMBER, it.toLong()) }
        metadata.year?.let { putLong(METADATA_KEY_YEAR, it.toLong()) }
        metadata.downloadStatus?.let { putLong(METADATA_KEY_DOWNLOAD_STATUS, toDownloadStatus(it)) }
        metadata.userRating?.let { toRatingCompat(it) }?.let {
            putRating(METADATA_KEY_RATING, it)
            putRating(METADATA_KEY_USER_RATING, it)
        }
        putLong(METADATA_KEY_ADVERTISEMENT, if (isAdvertisement) 1 else 0)
    }.build()

    private fun toRatingCompat(rating: MediaRating<*>): RatingCompat {
        return when (val scale = rating.scale) {
            RatingScale.HeartRatingScale -> {
                rating.value?.let {
                    newHeartRating(it is Hearted)
                } ?: newUnratedRating(RATING_HEART)
            }
            RatingScale.ThumbRatingScale -> {
                rating.value?.let {
                    newThumbRating(it is ThumbsUp)
                } ?: newUnratedRating(RATING_THUMB_UP_DOWN)
            }
            is RatingScale.StarRatingScale -> {
                (rating.value as StarRatingValue?)?.let {
                    newStarRating(
                        when (scale.maxNumberOfStars) {
                            3 -> RATING_3_STARS
                            4 -> RATING_4_STARS
                            5 -> RATING_5_STARS
                            else -> throw IllegalArgumentException(
                                "Invalid maxNumberOfStars (${scale.maxNumberOfStars})." +
                                    "Must be between 3 and 5."
                            )
                        },
                        it.numberOfStars.toFloat()
                    )
                } ?: newUnratedRating(RATING_THUMB_UP_DOWN)
            }
            RatingScale.PercentageRatingScale -> {
                (rating.value as PercentageRatingValue?)?.let {
                    newPercentageRating(it.percent.toFloat())
                } ?: newUnratedRating(RATING_PERCENTAGE)
            }
        }
    }

    private fun toDownloadStatus(downloadStatus: MediaDownloadStatus): Long {
        return when (downloadStatus) {
            MediaDownloadStatus.DOWNLOADED -> STATUS_DOWNLOADED
            MediaDownloadStatus.DOWNLOADING -> STATUS_DOWNLOADING
            MediaDownloadStatus.NOT_DOWNLOADED -> STATUS_NOT_DOWNLOADED
        }
    }

}
