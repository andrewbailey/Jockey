package dev.andrewbailey.music.library

import dev.andrewbailey.encore.provider.MergedMediaProvider
import dev.andrewbailey.encore.provider.WrappedMediaProvider
import dev.andrewbailey.encore.provider.mediastore.MediaStoreProvider
import dev.andrewbailey.music.model.Album
import dev.andrewbailey.music.model.Artist
import dev.andrewbailey.music.model.LocalAlbum
import dev.andrewbailey.music.model.LocalArtist
import dev.andrewbailey.music.model.LocalSong
import dev.andrewbailey.music.model.Song

class JockeyMediaRepository(
    private val mediaStoreProvider: MediaStoreProvider
) : MediaRepository, MergedMediaProvider<Song>(
    providers = listOf(
        WrappedMediaProvider(
            originId = LocalSong.ORIGIN_ID,
            provider = mediaStoreProvider,
            mergeConverter = { LocalSong(it) }
        )
    )
) {

    override suspend fun getAllSongs(): List<Song> {
        return query<MediaStoreProvider> { it.getAllSongs() }
    }

    override suspend fun getSongsInAlbum(album: Album): List<Song> {
        return when (album) {
            is LocalAlbum -> {
                query<MediaStoreProvider> { it.getSongsInAlbum(album.mediaStoreAlbum) }
            }
        }
    }

    override suspend fun getSongsByArtist(artist: Artist): List<Song> {
        return when (artist) {
            is LocalArtist -> {
                query<MediaStoreProvider> { it.getSongsByArtist(artist.mediaStoreArtist) }
            }
        }
    }

    override suspend fun getAllAlbums(): List<Album> {
        return mediaStoreProvider.getAllAlbums().map { LocalAlbum(it) }
    }

    override suspend fun getAlbumsByArtist(artist: Artist): List<Album> {
        return when (artist) {
            is LocalArtist -> {
                mediaStoreProvider.getAlbumsByArtist(artist.mediaStoreArtist).map { LocalAlbum(it) }
            }
        }
    }

    override suspend fun getAllArtists(): List<Artist> {
        return mediaStoreProvider.getAllArtists().map { LocalArtist(it) }
    }
}
