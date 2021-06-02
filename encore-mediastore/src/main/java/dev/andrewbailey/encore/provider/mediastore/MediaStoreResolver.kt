package dev.andrewbailey.encore.provider.mediastore

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import dev.andrewbailey.encore.provider.mediastore.entity.AlbumEntity
import dev.andrewbailey.encore.provider.mediastore.entity.ArtistEntity
import dev.andrewbailey.encore.provider.mediastore.entity.GenreContentsEntity
import dev.andrewbailey.encore.provider.mediastore.entity.GenreEntity
import dev.andrewbailey.encore.provider.mediastore.entity.SongEntity

internal class MediaStoreResolver(
    private val context: Context
) {

    // TODO: Many of the fields in the MediaProvider accessed are incorrectly flagged by the
    //  inlinedapi inspection as fields that are unavailable before API level 29. This is tracked
    //  in https://issuetracker.google.com/issues/175930197. After the linter is fixed, these
    //  suppressions should be removed.
    companion object {
        /**
         * Android's sqlite implementation has a fixed number of sqlite variables of 999. If a query
         * exceeds this many variables, an SQLiteException will be thrown. Queries with many
         * variables should be sure to keep their selection arguments underneath this value, which
         * has a bit of overhead taken off to account for variables injected by this class.
         *
         * The original value from the platform is defined as SQLITE_MAX_VARIABLE_NUMBER in
         * https://raw.githubusercontent.com/android/platform_external_sqlite/master/dist/sqlite3.c
         */
        internal const val MAX_SELECTION_ARGS = 990

        private val songProjection = listOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.ARTIST_ID,
            MediaStore.Audio.Media.ARTIST,
            // noinspection inlinedapi
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.YEAR,
            MediaStore.Audio.Media.TRACK,
        )

        private val albumProjection = listOfNotNull(
            MediaStore.Audio.Albums._ID,
            MediaStore.Audio.Albums.ALBUM,
            MediaStore.Audio.Albums.ARTIST,
            // noinspection inlinedapi
            MediaStore.Audio.Albums.ARTIST_ID
        )

        private val artistProjection = listOf(
            MediaStore.Audio.Artists._ID,
            MediaStore.Audio.Artists.ARTIST
        )

        private val genreProjection = listOf(
            MediaStore.Audio.Genres._ID,
            MediaStore.Audio.Genres.NAME
        )

        private val genreContentsProjection = listOf(
            MediaStore.Audio.Genres.Members.GENRE_ID,
            MediaStore.Audio.Genres.Members.AUDIO_ID
        )
    }

    fun queryAllSongs(
        uri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
    ): List<SongEntity> {
        val query = context.query(
            uri = uri,
            projection = songProjection,
            selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        )

        return query?.use { cursor -> cursor.toList { SongEntity.fromCursor(uri, it) } }
            .orEmpty()
    }

    fun querySongs(
        uri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
        selection: String,
        selectionArgs: List<String> = emptyList()
    ): List<SongEntity> {
        val query = context.query(
            uri = uri,
            projection = songProjection,
            selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND ($selection)",
            selectionArgs = selectionArgs.takeIf { it.isNotEmpty() }?.toTypedArray()
        )

        return query?.use { cursor -> cursor.toList { SongEntity.fromCursor(uri, it) } }
            .orEmpty()
    }

    fun queryAllAlbums(
        uri: Uri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI
    ): List<AlbumEntity> {
        val query = context.query(
            uri = uri,
            projection = albumProjection
        )

        return query?.use { cursor -> cursor.toList { AlbumEntity.fromCursor(it) } }
            .orEmpty()
    }

    fun queryAlbums(
        uri: Uri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
        selection: String,
        selectionArgs: List<String> = emptyList()
    ): List<AlbumEntity> {
        val query = context.query(
            uri = uri,
            projection = albumProjection,
            selection = selection,
            selectionArgs = selectionArgs.takeIf { it.isNotEmpty() }?.toTypedArray()
        )

        return query?.use { cursor -> cursor.toList { AlbumEntity.fromCursor(it) } }
            .orEmpty()
    }

    fun queryAllArtists(
        uri: Uri = MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI
    ): List<ArtistEntity> {
        val query = context.query(
            uri = uri,
            projection = artistProjection
        )

        return query?.use { cursor -> cursor.toList { ArtistEntity.fromCursor(it) } }
            .orEmpty()
    }

    fun queryArtists(
        uri: Uri = MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI,
        selection: String,
        selectionArgs: List<String> = emptyList()
    ): List<ArtistEntity> {
        val query = context.query(
            uri = uri,
            projection = artistProjection,
            selection = selection,
            selectionArgs = selectionArgs.takeIf { it.isNotEmpty() }?.toTypedArray()
        )

        return query?.use { cursor -> cursor.toList { ArtistEntity.fromCursor(it) } }
            .orEmpty()
    }

    fun queryAllGenres(
        uri: Uri = MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI,
    ): List<GenreEntity> {
        val query = context.query(
            uri = uri,
            projection = genreProjection
        )

        return query?.use { cursor -> cursor.toList { GenreEntity.fromCursor(it) } }
            .orEmpty()
    }

    fun queryGenres(
        uri: Uri = MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI,
        selection: String,
        selectionArgs: List<String> = emptyList()
    ): List<GenreEntity> {
        val query = context.query(
            uri = uri,
            projection = genreProjection,
            selection = selection,
            selectionArgs = selectionArgs.takeIf { it.isNotEmpty() }?.toTypedArray()
        )

        return query?.use { cursor -> cursor.toList { GenreEntity.fromCursor(it) } }
            .orEmpty()
    }

    fun queryAllGenreContents(
        // This value comes from MediaStore.VOLUME_EXTERNAL. It is inlined here because this
        // constant it is not available on all target APIs.
        volumeName: String = "external",
        genreId: Long
    ): List<GenreContentsEntity> {
        val query = context.query(
            uri = MediaStore.Audio.Genres.Members.getContentUri(volumeName, genreId),
            projection = genreContentsProjection
        )

        return query?.use { cursor -> cursor.toList { GenreContentsEntity.fromCursor(cursor) } }
            .orEmpty()
    }

    fun queryGenreContents(
        // This value comes from MediaStore.VOLUME_EXTERNAL. It is inlined here because this
        // constant it is not available on all target APIs.
        volumeName: String = "external",
        genreId: Long,
        selection: String,
        selectionArgs: List<String> = emptyList()
    ): List<GenreContentsEntity> {
        val query = context.query(
            uri = MediaStore.Audio.Genres.Members.getContentUri(volumeName, genreId),
            projection = genreContentsProjection,
            selection = selection,
            selectionArgs = selectionArgs.takeIf { it.isNotEmpty() }?.toTypedArray()
        )

        return query?.use { cursor -> cursor.toList { GenreContentsEntity.fromCursor(cursor) } }
            .orEmpty()
    }

}
