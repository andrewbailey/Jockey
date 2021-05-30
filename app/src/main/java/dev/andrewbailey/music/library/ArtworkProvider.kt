package dev.andrewbailey.music.library

import coil.ComponentRegistry

interface ArtworkProvider {

    fun ComponentRegistry.Builder.install()

}

fun ComponentRegistry.Builder.install(artworkProvider: ArtworkProvider) {
    with(artworkProvider) {
        install()
    }
}
