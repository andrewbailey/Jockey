package dev.andrewbailey.encore.provider.mediastore.artwork

import android.graphics.drawable.BitmapDrawable
import coil.ComponentRegistry
import coil.bitmap.BitmapPool
import coil.decode.DataSource
import coil.decode.Options
import coil.fetch.DrawableResult
import coil.fetch.FetchResult
import coil.fetch.Fetcher
import coil.size.OriginalSize
import coil.size.PixelSize
import coil.size.Size
import dev.andrewbailey.encore.provider.mediastore.MediaStoreAlbum
import dev.andrewbailey.encore.provider.mediastore.MediaStoreSong
import java.io.IOException

public fun ComponentRegistry.Builder.installMediaStoreFetchers(
    artworkProvider: MediaStoreArtworkProvider
) {
    add(MediaStoreCoilAlbumFetcher(artworkProvider))
    add(MediaStoreCoilSongFetcher(artworkProvider))
}

private class MediaStoreCoilAlbumFetcher(
    private val mediaStoreArtworkProvider: MediaStoreArtworkProvider
) : Fetcher<MediaStoreAlbum> {

    override suspend fun fetch(
        pool: BitmapPool,
        data: MediaStoreAlbum,
        size: Size,
        options: Options
    ): FetchResult = DrawableResult(
        drawable = BitmapDrawable(
            options.context.resources,
            mediaStoreArtworkProvider.getAlbumArtwork(
                album = data,
                widthPx = when (size) {
                    OriginalSize -> null
                    is PixelSize -> size.width
                },
                heightPx = when (size) {
                    OriginalSize -> null
                    is PixelSize -> size.height
                }
            )
        ),
        isSampled = true,
        dataSource = DataSource.DISK
    )

    override fun key(data: MediaStoreAlbum): String = data.id

}

private class MediaStoreCoilSongFetcher(
    private val mediaStoreArtworkProvider: MediaStoreArtworkProvider
) : Fetcher<MediaStoreSong> {

    override suspend fun fetch(
        pool: BitmapPool,
        data: MediaStoreSong,
        size: Size,
        options: Options
    ): FetchResult = DrawableResult(
        drawable = BitmapDrawable(
            options.context.resources,
            mediaStoreArtworkProvider.getEmbeddedArtwork(
                song = data,
                widthPx = when (size) {
                    OriginalSize -> null
                    is PixelSize -> size.width
                },
                heightPx = when (size) {
                    OriginalSize -> null
                    is PixelSize -> size.height
                }
            ) ?: throw IOException("${data.playbackUri} does not have embedded artwork")
        ),
        isSampled = true,
        dataSource = DataSource.DISK
    )

    override fun key(data: MediaStoreSong): String = data.id

}
