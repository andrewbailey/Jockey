package dev.andrewbailey.encore.provider

import dev.andrewbailey.encore.model.MediaItem

public interface MediaProvider<out M : MediaItem> {

    public suspend fun getMediaItemById(id: String): M?

    public suspend fun getMediaItemsByIds(ids: List<String>): List<M>

    public suspend fun searchForMediaItems(query: String): List<M>

}
