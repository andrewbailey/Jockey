package dev.andrewbailey.encore.provider

import dev.andrewbailey.encore.model.MediaAuthor
import dev.andrewbailey.encore.model.MediaCollection
import dev.andrewbailey.encore.model.MediaItem

interface MediaProvider {

    suspend fun getAllMedia(): List<MediaItem>

    suspend fun getMediaById(id: String): MediaItem?

    suspend fun getAllCollections(): List<MediaCollection>

    suspend fun getCollectionById(id: String): MediaCollection?

    suspend fun getMediaInCollection(collection: MediaCollection): List<MediaItem>

    suspend fun getAuthors(): List<MediaAuthor>

    suspend fun getAuthorById(id: String): MediaAuthor?

    suspend fun getCollectionsByAuthor(author: MediaAuthor): List<MediaCollection>

    suspend fun getMediaByAuthor(author: MediaAuthor): List<MediaItem>

}
