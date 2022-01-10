package dev.andrewbailey.encore.provider.mediastore.artwork

import android.graphics.drawable.BitmapDrawable
import coil.ComponentRegistry
import coil.ImageLoader
import coil.decode.DataSource
import coil.fetch.DrawableResult
import coil.fetch.Fetcher
import coil.request.Options
import coil.size.pxOrElse
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
) : Fetcher.Factory<MediaStoreAlbum> {

    override fun create(
        data: MediaStoreAlbum,
        options: Options,
        imageLoader: ImageLoader
    ) = Fetcher {
        DrawableResult(
            drawable = BitmapDrawable(
                options.context.resources,
                mediaStoreArtworkProvider.getAlbumArtwork(
                    album = data,
                    widthPx = options.size.width.pxOrElse { 0 }.takeIf { it > 0 },
                    heightPx = options.size.height.pxOrElse { 0 }.takeIf { it > 0 }
                )
            ),
            isSampled = true,
            dataSource = DataSource.DISK
        )
    }

}

private class MediaStoreCoilSongFetcher(
    private val mediaStoreArtworkProvider: MediaStoreArtworkProvider
) : Fetcher.Factory<MediaStoreSong> {

    override fun create(
        data: MediaStoreSong,
        options: Options,
        imageLoader: ImageLoader
    ) = Fetcher {
        DrawableResult(
            drawable = BitmapDrawable(
                options.context.resources,
                mediaStoreArtworkProvider.getEmbeddedArtwork(
                    song = data,
                    widthPx = options.size.width.pxOrElse { 0 }.takeIf { it > 0 },
                    heightPx = options.size.height.pxOrElse { 0 }.takeIf { it > 0 }
                ) ?: throw IOException("${data.playbackUri} does not have embedded artwork")
            ),
            isSampled = true,
            dataSource = DataSource.DISK
        )
    }

}
