package dev.andrewbailey.encore.model

import dev.andrewbailey.annotations.compose.ComposeStableClass
import java.time.Instant

@ComposeStableClass
public data class MediaMetadata(
    val title: String,
    val subtitle: String? = null,
    val description: String? = null,
    val artistName: String? = null,
    val albumName: String? = null,
    val albumArtistName: String? = null,
    val authorName: String? = null,
    val writerName: String? = null,
    val composerName: String? = null,
    val publishedAt: Instant? = null,
    val genreName: String? = null,
    val trackNumber: Int? = null,
    val numberOfTracks: Int? = null,
    val discNumber: Int? = null,
    val year: Int? = null,
    val durationMs: Long? = null,
    val artworkUri: String? = null,
    val userRating: MediaRating<*>? = null,
    val downloadStatus: MediaDownloadStatus? = null
)
