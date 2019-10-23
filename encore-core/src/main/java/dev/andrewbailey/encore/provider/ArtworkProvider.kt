package dev.andrewbailey.encore.provider

import android.graphics.Bitmap
import androidx.annotation.Px
import dev.andrewbailey.encore.model.MediaAuthor
import dev.andrewbailey.encore.model.MediaCollection
import dev.andrewbailey.encore.model.MediaItem

interface ArtworkProvider {

    suspend fun getArtworkForMediaItem(
        mediaItem: MediaItem,
        @Px widthPx: Int? = null,
        @Px heightPx: Int? = null
    ): Bitmap?

    suspend fun getArtworkForMediaAuthor(
        mediaAuthor: MediaAuthor,
        @Px widthPx: Int? = null,
        @Px heightPx: Int? = null
    ): Bitmap?

    suspend fun getArtworkForMediaCollection(
        mediaCollection: MediaCollection,
        @Px widthPx: Int? = null,
        @Px heightPx: Int? = null
    ): Bitmap?

}
