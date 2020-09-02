package dev.andrewbailey.encore.provider

import dev.andrewbailey.encore.model.MediaAuthor
import dev.andrewbailey.encore.model.MediaCollection
import dev.andrewbailey.encore.model.MediaItem

public interface MediaProvider {

    public suspend fun getAllMedia(): List<MediaItem>

    public suspend fun getMediaById(id: String): MediaItem?

    public suspend fun getAllCollections(): List<MediaCollection>

    public suspend fun getCollectionById(id: String): MediaCollection?

    public suspend fun getMediaInCollection(collection: MediaCollection): List<MediaItem>

    public suspend fun getAuthors(): List<MediaAuthor>

    public suspend fun getAuthorById(id: String): MediaAuthor?

    public suspend fun getCollectionsByAuthor(author: MediaAuthor): List<MediaCollection>

    public suspend fun getMediaByAuthor(author: MediaAuthor): List<MediaItem>

}
