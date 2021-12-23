package dev.andrewbailey.encore.provider.mediastore

import android.content.Context
import android.provider.MediaStore
import dev.andrewbailey.encore.model.MediaSearchArguments
import dev.andrewbailey.encore.provider.MediaField.Author
import dev.andrewbailey.encore.provider.MediaField.Collection
import dev.andrewbailey.encore.provider.MediaField.Genre
import dev.andrewbailey.encore.provider.MediaField.Title
import dev.andrewbailey.encore.provider.MediaProvider
import dev.andrewbailey.encore.provider.MediaSearchResults
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

public class MediaStoreProvider(
    context: Context
) : MediaProvider<MediaStoreSong> {

    private val mediaStore = MediaStoreResolver(context.applicationContext)

    override suspend fun getMediaItemsByIds(ids: List<String>): List<MediaStoreSong> {
        return withContext(Dispatchers.IO) {
            val allGenres = mediaStore.queryAllGenres()

            ids.chunked(MediaStoreResolver.MAX_SELECTION_ARGS)
                .flatMap { idsSubset ->
                    MediaStoreMapper.toMediaItems(
                        songEntities = mediaStore.querySongs(
                            selection = idsSubset.asSequence()
                                .map { "${MediaStore.Audio.Media._ID} = ?" }
                                .joinToString(separator = " OR "),
                            selectionArgs = idsSubset
                        ),
                        genreEntities = allGenres,
                        genreContents = allGenres.mapNotNull { it.id }
                            .flatMap { genreId ->
                                mediaStore.queryGenreContents(
                                    genreId = genreId,
                                    selection = idsSubset.asSequence()
                                        .map { "${MediaStore.Audio.Genres.Members.AUDIO_ID} = ?" }
                                        .joinToString(separator = " OR "),
                                    selectionArgs = idsSubset
                                )
                            }
                    )
                }
        }
    }

    override suspend fun searchForMediaItems(
        query: String,
        arguments: MediaSearchArguments
    ): MediaSearchResults<MediaStoreSong> {
        val allSongs = getAllSongs()

        val searchResults = allSongs
            .filter { song ->
                song.name.contains(arguments.fields[Title] ?: query, true) ||
                    song.genre?.name.orEmpty().contains(arguments.fields[Genre] ?: query, true) ||
                    song.artist?.name.orEmpty().contains(arguments.fields[Author] ?: query, true) ||
                    song.album?.name.orEmpty().contains(arguments.fields[Collection] ?: query, true)
            }

        val playbackContinuation = when (arguments.preferredSearchField) {
            Title, Collection -> {
                // Include other songs by the artists in the search results
                searchResults.asSequence()
                    .mapNotNull { it.artist }
                    .toSet()
                    .flatMap { getSongsByArtist(it) }
                    .filter { it !in searchResults }
            }
            Genre -> emptyList()
            Author, null -> {
                // Include other songs in the same genre in the search results
                searchResults.asSequence()
                    .mapNotNull { it.genre }
                    .toSet()
                    .flatMap { genre -> allSongs.filter { it.genre == genre } }
                    .filter { it !in searchResults }
            }
        }

        // TODO: Search results should be sorted with better matches appearing first.
        return MediaSearchResults(
            searchResults = searchResults,
            playbackContinuation = playbackContinuation
        )
    }

    override suspend fun getMediaItemById(id: String): MediaStoreSong? {
        return withContext(Dispatchers.IO) {
            val song = mediaStore.querySongs(
                selection = "${MediaStore.Audio.Media._ID} = ?",
                selectionArgs = listOf(id)
            ).firstOrNull() ?: return@withContext null

            val genreId = mediaStore.queryAllGenres()
                .asSequence()
                .mapNotNull { it.id }
                .filter { genreId ->
                    mediaStore.queryGenreContents(
                        genreId = genreId,
                        selection = "${MediaStore.Audio.Genres.Members.AUDIO_ID} = ?",
                        selectionArgs = listOf(song.id.toString())
                    ).isNotEmpty()
                }
                .firstOrNull()

            val genre = genreId?.let {
                mediaStore.queryGenres(
                    selection = "${MediaStore.Audio.Genres._ID} = ?",
                    selectionArgs = listOf(genreId.toString())
                )
            }?.firstOrNull()

            MediaStoreMapper.toMediaItem(
                songEntity = song,
                genre = genre?.let { MediaStoreMapper.toMediaStoreGenre(it) }
            )
        }
    }

    public suspend fun getAllSongs(): List<MediaStoreSong> {
        return withContext(Dispatchers.IO) {
            val allGenres = mediaStore.queryAllGenres()
            MediaStoreMapper.toMediaItems(
                songEntities = mediaStore.queryAllSongs(),
                genreEntities = allGenres,
                genreContents = allGenres
                    .mapNotNull { it.id }
                    .flatMap { genreId ->
                        mediaStore.queryAllGenreContents(genreId = genreId)
                    }
            ).sortedByTitle()
        }
    }

    public suspend fun getAllAlbums(): List<MediaStoreAlbum> {
        return withContext(Dispatchers.IO) {
            mediaStore.queryAllAlbums()
                .map { MediaStoreMapper.toMediaCollection(it) }
                .sortedByName()
        }
    }

    public suspend fun getAlbumById(id: String): MediaStoreAlbum? {
        return withContext(Dispatchers.IO) {
            mediaStore.queryAlbums(
                selection = "${MediaStore.Audio.Albums._ID} = ?",
                selectionArgs = listOf(id)
            ).firstOrNull()
                ?.let { MediaStoreMapper.toMediaCollection(it) }
        }
    }

    public suspend fun getSongsInAlbum(album: MediaStoreAlbum): List<MediaStoreSong> {
        return withContext(Dispatchers.IO) {
            val songs = mediaStore.querySongs(
                selection = "${MediaStore.Audio.Media.ALBUM_ID} = ?",
                selectionArgs = listOf(album.id)
            )

            val allGenres = mediaStore.queryAllGenres()

            val genreLookup = songs.asSequence()
                .map { it.id.toString() }
                .chunked(MediaStoreResolver.MAX_SELECTION_ARGS)
                .flatMap { songIdsSubset ->
                    allGenres.mapNotNull { it.id }
                        .flatMap { genreId ->
                            mediaStore.queryGenreContents(
                                genreId = genreId,
                                selection = songIdsSubset.asSequence()
                                    .map { "${MediaStore.Audio.Genres.Members.AUDIO_ID} = ?" }
                                    .joinToString(separator = " OR "),
                                selectionArgs = songIdsSubset
                            )
                        }
                }
                .toList()

            MediaStoreMapper.toMediaItems(
                songEntities = songs,
                genreEntities = allGenres,
                genreContents = genreLookup
            )
        }
    }

    public suspend fun getAllArtists(): List<MediaStoreArtist> {
        return withContext(Dispatchers.IO) {
            mediaStore.queryAllArtists()
                .map { MediaStoreMapper.toMediaAuthor(it) }
                .sortedByName()
        }
    }

    public suspend fun getArtistById(id: String): MediaStoreArtist? {
        return withContext(Dispatchers.IO) {
            mediaStore.queryArtists(
                selection = "${MediaStore.Audio.Artists._ID} = ?",
                selectionArgs = listOf(id)
            ).firstOrNull()
                ?.let {
                    MediaStoreMapper.toMediaAuthor(
                        it
                    )
                }
        }
    }

    public suspend fun getAlbumsByArtist(artist: MediaStoreArtist): List<MediaStoreAlbum> {
        return withContext(Dispatchers.IO) {
            mediaStore.queryAlbums(
                // noinspection inlinedapi
                selection = "${MediaStore.Audio.Albums.ARTIST_ID} = ?",
                selectionArgs = listOf(artist.id)
            ).map { MediaStoreMapper.toMediaCollection(it) }
        }
    }

    public suspend fun getSongsByArtist(artist: MediaStoreArtist): List<MediaStoreSong> {
        return withContext(Dispatchers.IO) {
            val songs = mediaStore.querySongs(
                selection = "${MediaStore.Audio.Media.ARTIST_ID} = ?",
                selectionArgs = listOf(artist.id)
            )

            val allGenres = mediaStore.queryAllGenres()

            val genreLookup = songs.asSequence()
                .map { it.id.toString() }
                .chunked(MediaStoreResolver.MAX_SELECTION_ARGS)
                .flatMap { songIdsSubset ->
                    allGenres.mapNotNull { it.id }.flatMap { genreId ->
                        mediaStore.queryGenreContents(
                            genreId = genreId,
                            selection = songIdsSubset.asSequence()
                                .map { "${MediaStore.Audio.Genres.Members.AUDIO_ID} = ?" }
                                .joinToString(separator = " OR "),
                            selectionArgs = songIdsSubset
                        )
                    }
                }
                .toList()

            MediaStoreMapper.toMediaItems(
                songEntities = songs,
                genreEntities = allGenres,
                genreContents = genreLookup
            )
        }
    }

    public suspend fun getAllPlaylists(): List<MediaStorePlaylist> {
        return withContext(Dispatchers.IO) {
            mediaStore.queryAllPlaylists()
                .map { MediaStoreMapper.toMediaStorePlaylist(it) }
                .sortedByName()
        }
    }

}
