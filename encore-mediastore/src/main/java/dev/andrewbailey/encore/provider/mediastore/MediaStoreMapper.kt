package dev.andrewbailey.encore.provider.mediastore

import android.net.Uri
import dev.andrewbailey.encore.provider.mediastore.entity.AlbumEntity
import dev.andrewbailey.encore.provider.mediastore.entity.ArtistEntity
import dev.andrewbailey.encore.provider.mediastore.entity.SongEntity

internal object MediaStoreMapper {

    fun toMediaItem(songEntity: SongEntity) = LocalSong(
        id = songEntity.id,
        playbackUri = Uri.withAppendedPath(songEntity.contentUri, songEntity.id).toString(),
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
        )
    )

    private fun makeArtist(
        id: String?,
        name: String?
    ): LocalArtist? {
        return LocalArtist(
            id = id ?: return null,
            name = name ?: return null
        )
    }

    private fun makeAlbum(
        id: String?,
        name: String?,
        artist: LocalArtist?
    ): LocalAlbum? {
        return LocalAlbum(
            id = id ?: return null,
            name = name ?: return null,
            author = artist
        )
    }

    fun toMediaCollection(
        albumEntity: AlbumEntity,
        fallbackArtistIdLookup: (AlbumEntity) -> String?
    ) = LocalAlbum(
        id = albumEntity.id,
        name = albumEntity.title.orEmpty(),
        author = makeArtist(
            id = albumEntity.artistId ?: fallbackArtistIdLookup(albumEntity),
            name = albumEntity.artistName
        )
    )

    fun toMediaAuthor(artistEntity: ArtistEntity) = LocalArtist(
        id = artistEntity.id,
        name = artistEntity.name.orEmpty()
    )

}
