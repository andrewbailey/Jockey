package dev.andrewbailey.encore.provider.mediastore

import dev.andrewbailey.encore.provider.mediastore.entity.AlbumEntity
import dev.andrewbailey.encore.provider.mediastore.entity.ArtistEntity
import dev.andrewbailey.encore.provider.mediastore.entity.GenreContentsEntity
import dev.andrewbailey.encore.provider.mediastore.entity.GenreEntity
import dev.andrewbailey.encore.provider.mediastore.entity.SongEntity

internal object MediaStoreMapper {

    fun toMediaItems(
        songEntities: List<SongEntity>,
        genreEntities: List<GenreEntity>,
        genreContents: List<GenreContentsEntity>
    ): List<MediaStoreSong> {
        val genres = genreEntities.mapNotNull { makeGenre(id = it.id.toString(), name = it.name) }
        val genreMapping = genreContents.map { genreAssociation ->
            genreAssociation.songId to genres.first { it.id == genreAssociation.genreId.toString() }
        }.toMap()

        return songEntities.map { songEntity ->
            toMediaItem(
                songEntity = songEntity,
                genre = genreMapping[songEntity.id]
            )
        }
    }

    fun toMediaItem(
        songEntity: SongEntity,
        genre: MediaStoreGenre?
    ) = MediaStoreSong(
        id = songEntity.id.toString(),
        playbackUri = songEntity.contentUri.buildUpon()
            .appendEncodedPath(songEntity.id.toString())
            .toString(),
        name = songEntity.title.orEmpty(),
        artist = makeArtist(
            id = songEntity.artistId,
            name = songEntity.artistName
        ),
        album = makeAlbum(
            id = songEntity.albumId,
            name = songEntity.albumName,
            artist = makeArtist(
                id = songEntity.artistId,
                name = songEntity.artistName
            )
        ),
        genre = genre,
        /*
            MediaStore encodes the number of tracks on an album as XYYY, where X is the disc number
            and YYY is the three least-significant digits of the track number padded with zeroes.
            This encoding fails to accurately account for albums with more than 999 songs on them.
            In a scenario where a song is track 2560 on disc 11, the `track` value we read from the
            MediaStore will be 13560. It's impossible to discern when this scenario has occurred
            using the MediaStore alone.

            TODO: Consider reading these fields from the media file directly instead of depending
                  on the MediaStore's values, which will provide more accuracy at the expense of
                  performance.
         */
        trackNumber = songEntity.track?.let { it % 1000 }?.takeIf { it > 0 },
        discNumber = songEntity.track?.let { it / 1000 },
        publishYear = songEntity.publishYear,
        durationMs = songEntity.durationMs
    )

    private fun makeArtist(
        id: String?,
        name: String?
    ): MediaStoreArtist? {
        return MediaStoreArtist(
            id = id ?: return null,
            name = name ?: return null
        )
    }

    private fun makeAlbum(
        id: String?,
        name: String?,
        artist: MediaStoreArtist?
    ): MediaStoreAlbum? {
        return MediaStoreAlbum(
            id = id ?: return null,
            name = name ?: return null,
            author = artist
        )
    }

    private fun makeGenre(
        id: String?,
        name: String?
    ): MediaStoreGenre? {
        return MediaStoreGenre(
            id = id ?: return null,
            name = name ?: return null
        )
    }

    fun toMediaCollection(
        albumEntity: AlbumEntity,
    ) = MediaStoreAlbum(
        id = albumEntity.id.toString(),
        name = albumEntity.title.orEmpty(),
        author = makeArtist(
            id = albumEntity.artistId,
            name = albumEntity.artistName
        )
    )

    fun toMediaAuthor(artistEntity: ArtistEntity) = MediaStoreArtist(
        id = artistEntity.id.toString(),
        name = artistEntity.name.orEmpty()
    )

    fun toMediaStoreGenre(genreEntity: GenreEntity) = MediaStoreGenre(
        id = genreEntity.id.toString(),
        name = genreEntity.name.orEmpty()
    )

}
