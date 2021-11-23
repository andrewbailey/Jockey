package dev.andrewbailey.encore.provider.mediastore.artwork

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import dev.andrewbailey.encore.provider.mediastore.MediaStoreAlbum
import dev.andrewbailey.encore.provider.mediastore.MediaStoreSong
import java.lang.RuntimeException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

public class MediaStoreArtworkProvider(
    private val context: Context,
    defaultImageSizePx: Int = 512
) {

    private val artworkResolver = MediaStoreArtworkResolver.obtain(
        context = context,
        defaultImageSizePx = defaultImageSizePx
    )

    public suspend fun getEmbeddedArtwork(
        song: MediaStoreSong,
        widthPx: Int?,
        heightPx: Int?
    ): Bitmap? = withContext(Dispatchers.IO) {
        try {
            MediaMetadataRetriever().use { retriever ->
                retriever.setDataSource(context, Uri.parse(song.playbackUri))
                retriever.embeddedPicture?.let { picture ->
                    BitmapFactory.decodeByteArray(
                        picture,
                        0,
                        picture.size,
                        BitmapFactory.Options().apply {
                            inSampleSize = determineScaleFactor(
                                bytes = picture,
                                widthPx = widthPx,
                                heightPx = heightPx
                            )
                        }
                    )
                }
            }
        } catch (e: RuntimeException) {
            Log.e(TAG, "Failed to load full song artwork", e)
            null
        } catch (e: OutOfMemoryError) {
            Log.e(TAG, "Unable to allocate space on the heap for full song artwork", e)
            null
        }
    }

    public suspend fun getArtworkThumbnail(
        song: MediaStoreSong,
        widthPx: Int,
        heightPx: Int
    ): Bitmap? = artworkResolver.getThumbnail(song, widthPx, heightPx)

    public suspend fun getAlbumArtwork(
        album: MediaStoreAlbum,
        widthPx: Int?,
        heightPx: Int?
    ): Bitmap? = artworkResolver.getThumbnail(album, widthPx, heightPx)

    private companion object {
        private const val TAG = "MediaStoreArtProvider"
    }

}
