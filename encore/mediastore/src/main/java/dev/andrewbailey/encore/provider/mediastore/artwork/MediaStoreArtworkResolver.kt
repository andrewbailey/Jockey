package dev.andrewbailey.encore.provider.mediastore.artwork

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.CancellationSignal
import android.provider.MediaStore
import android.util.Size
import androidx.annotation.RequiresApi
import dev.andrewbailey.encore.provider.mediastore.MediaStoreAlbum
import dev.andrewbailey.encore.provider.mediastore.MediaStoreSong
import dev.andrewbailey.encore.provider.mediastore.query
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext

internal sealed class MediaStoreArtworkResolver {

    abstract suspend fun getThumbnail(
        song: MediaStoreSong,
        widthPx: Int?,
        heightPx: Int?
    ): Bitmap?

    abstract suspend fun getThumbnail(
        album: MediaStoreAlbum,
        widthPx: Int?,
        heightPx: Int?
    ): Bitmap?

    companion object {
        fun obtain(
            context: Context,
            defaultImageSizePx: Int
        ): MediaStoreArtworkResolver {
            return if (Build.VERSION.SDK_INT >= 29) {
                MediaStoreArtworkResolverApi29(context, defaultImageSizePx)
            } else {
                MediaStoreArtworkResolverApi16(context)
            }
        }
    }
}

private class MediaStoreArtworkResolverApi16(
    private val context: Context
) : MediaStoreArtworkResolver() {
    override suspend fun getThumbnail(
        song: MediaStoreSong,
        widthPx: Int?,
        heightPx: Int?
    ): Bitmap? = song.album?.let { getThumbnail(it, widthPx, heightPx) }

    override suspend fun getThumbnail(
        album: MediaStoreAlbum,
        widthPx: Int?,
        heightPx: Int?
    ): Bitmap? = withContext(Dispatchers.IO) {
        context.query(
            uri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
            projection = listOf(@Suppress("DEPRECATION") MediaStore.Audio.AlbumColumns.ALBUM_ART),
            selection = MediaStore.Audio.Albums._ID + " = ?",
            selectionArgs = arrayOf(album.id)
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                loadScaledBitmap(
                    path = cursor.getString(0),
                    widthPx = widthPx,
                    heightPx = heightPx
                )
            } else {
                null
            }
        }
    }

    private fun loadScaledBitmap(
        path: String,
        widthPx: Int?,
        heightPx: Int?
    ): Bitmap {
        val imageScaleFactor = determineScaleFactor(path, widthPx, heightPx)

        return BitmapFactory.decodeFile(
            path,
            BitmapFactory.Options().apply {
                inSampleSize = imageScaleFactor
            }
        )
    }
}

@RequiresApi(29)
private class MediaStoreArtworkResolverApi29(
    private val context: Context,
    private val defaultImageSizePx: Int
) : MediaStoreArtworkResolver() {

    override suspend fun getThumbnail(
        song: MediaStoreSong,
        widthPx: Int?,
        heightPx: Int?
    ): Bitmap? {
        return loadThumbnail(
            uri = Uri.parse(song.playbackUri),
            widthPx = widthPx,
            heightPx = heightPx
        )
    }

    override suspend fun getThumbnail(
        album: MediaStoreAlbum,
        widthPx: Int?,
        heightPx: Int?
    ): Bitmap? {
        return loadThumbnail(
            uri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI.buildUpon()
                .appendEncodedPath(album.id)
                .build(),
            widthPx = widthPx,
            heightPx = heightPx
        )
    }

    private suspend fun loadThumbnail(
        uri: Uri,
        widthPx: Int?,
        heightPx: Int?
    ): Bitmap? = suspendCancellableCoroutine { continuation ->
        val cancellationSignal = CancellationSignal()
        continuation.invokeOnCancellation { cancellationSignal.cancel() }
        try {
            val bitmap = context.contentResolver.loadThumbnail(
                uri,
                Size(widthPx ?: defaultImageSizePx, heightPx ?: defaultImageSizePx),
                cancellationSignal
            )

            continuation.resumeWith(Result.success(bitmap))
        } catch (e: IOException) {
            continuation.resumeWith(Result.failure(e))
        }
    }

}
