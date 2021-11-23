package dev.andrewbailey.encore.provider.mediastore

import dev.andrewbailey.encore.model.MediaDownloadStatus
import dev.andrewbailey.encore.model.MediaMetadata
import dev.andrewbailey.encore.model.MediaObject
import kotlinx.parcelize.Parcelize

@Parcelize
public data class MediaStoreSong(
    override val id: String,
    override val playbackUri: String,
    val name: String,
    val artist: MediaStoreArtist?,
    val album: MediaStoreAlbum?,
    val genre: MediaStoreGenre?,
    val trackNumber: Int?,
    val discNumber: Int?,
    val publishYear: Int?,
    val durationMs: Long?,
) : MediaObject {

    override fun toMediaMetadata(): MediaMetadata = MediaMetadata(
        title = name,
        artistName = artist?.name,
        albumName = album?.name,
        authorName = artist?.name,
        genreName = genre?.name,
        trackNumber = trackNumber,
        discNumber = discNumber,
        year = publishYear,
        durationMs = durationMs,
        downloadStatus = MediaDownloadStatus.DOWNLOADED
    )

}
