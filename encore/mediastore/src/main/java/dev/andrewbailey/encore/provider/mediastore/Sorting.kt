package dev.andrewbailey.encore.provider.mediastore

import java.text.Collator

@JvmName("sortedBySongTitle")
internal fun List<MediaStoreSong>.sortedByTitle() =
    sortedAlphabetically { it.name }

@JvmName("sortedByAlbumName")
internal fun List<MediaStoreAlbum>.sortedByName() =
    sortedAlphabetically { it.name }

@JvmName("sortedByArtistName")
internal fun List<MediaStoreArtist>.sortedByName() =
    sortedAlphabetically { it.name }

@JvmName("sortedByPlaylistName")
internal fun List<MediaStorePlaylist>.sortedByName() =
    sortedAlphabetically { it.name }

private inline fun <T> Iterable<T>.sortedAlphabetically(
    crossinline keySelector: (T) -> String
): List<T> {
    val collator = Collator.getInstance()
    return sortedWith { first, second ->
        collator.compare(keySelector(first), keySelector(second))
    }
}
