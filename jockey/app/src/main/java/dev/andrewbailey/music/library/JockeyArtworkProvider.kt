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

        add(Mapper<LocalSong, MediaStoreSong> { data, _ -> data.mediaStoreSong })

        add(Mapper<LocalAlbum, MediaStoreAlbum> { data, _ -> data.mediaStoreAlbum })
    }

}
