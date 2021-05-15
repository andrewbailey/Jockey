package dev.andrewbailey.music.library

import dev.andrewbailey.encore.provider.MediaProvider
import dev.andrewbailey.music.model.Album
import dev.andrewbailey.music.model.Artist
import dev.andrewbailey.music.model.Song

interface MediaRepository : MediaProvider<Song> {

    suspend fun getAllSongs(): List<Song>

    suspend fun getSongsInAlbum(album: Album): List<Song>

    suspend fun getSongsByArtist(artist: Artist): List<Song>

    suspend fun getAllAlbums(): List<Album>

    suspend fun getAlbumsByArtist(artist: Artist): List<Album>

    suspend fun getAllArtists(): List<Artist>

}
