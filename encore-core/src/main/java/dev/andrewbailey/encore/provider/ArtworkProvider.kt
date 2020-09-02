package dev.andrewbailey.encore.provider

import android.graphics.Bitmap
import androidx.annotation.Px
import dev.andrewbailey.encore.model.MediaAuthor
import dev.andrewbailey.encore.model.MediaCollection
import dev.andrewbailey.encore.model.MediaItem

public interface ArtworkProvider {

    public suspend fun getArtworkForMediaItem(
        mediaItem: MediaItem,
        @Px widthPx: Int? = null,
        @Px heightPx: Int? = null
    ): Bitmap?

    public suspend fun getArtworkForMediaAuthor(
        mediaAuthor: MediaAuthor,
        @Px widthPx: Int? = null,
        @Px heightPx: Int? = null
    ): Bitmap?

    public suspend fun getArtworkForMediaCollection(
        mediaCollection: MediaCollection,
        @Px widthPx: Int? = null,
        @Px heightPx: Int? = null
    ): Bitmap?

}
