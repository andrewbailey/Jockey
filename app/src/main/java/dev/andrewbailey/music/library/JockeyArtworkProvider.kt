package dev.andrewbailey.music.library

import coil.ComponentRegistry
import coil.map.Mapper
import dev.andrewbailey.encore.provider.mediastore.MediaStoreAlbum
import dev.andrewbailey.encore.provider.mediastore.MediaStoreSong
import dev.andrewbailey.encore.provider.mediastore.artwork.MediaStoreArtworkProvider
import dev.andrewbailey.encore.provider.mediastore.artwork.installMediaStoreFetchers
import dev.andrewbailey.music.model.LocalAlbum
import dev.andrewbailey.music.model.LocalSong
import javax.inject.Inject

class JockeyArtworkProvider @Inject constructor(
    private val mediaStoreArtworkProvider: MediaStoreArtworkProvider
) : ArtworkProvider {

    override fun ComponentRegistry.Builder.install() {
        installMediaStoreFetchers(mediaStoreArtworkProvider)

        add(object : Mapper<LocalSong, MediaStoreSong> {
            override fun map(data: LocalSong): MediaStoreSong = data.mediaStoreSong
        })

        add(object : Mapper<LocalAlbum, MediaStoreAlbum> {
            override fun map(data: LocalAlbum) = data.mediaStoreAlbum
        })
    }

}
