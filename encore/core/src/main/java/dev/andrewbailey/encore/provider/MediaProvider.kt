package dev.andrewbailey.encore.provider

import dev.andrewbailey.encore.model.MediaObject
import dev.andrewbailey.encore.model.MediaSearchArguments

public interface MediaProvider<out M : MediaObject> {

    public suspend fun getMediaItemById(id: String): M?

    public suspend fun getMediaItemsByIds(ids: List<String>): List<M>

    /**
     * Returns a collection of playable media items that match the given query. See
     * [MediaSearchResults] for more information on the expected return values.
     *
     * Implementors may decide how many results are included at most in these results. Implementors
     * are also responsible for making decisions about how best to interpret and compare against the
     * [query]. The [arguments] field can be used to improve the quality of this interpretation.
     *
     * Return results should be roughly sorted by the quality of the match, with better search
     * results appearing earlier in the list (if possible). It is up to the implementor to decide
     * how this match quality is determined.
     *
     * @param query A user-inputted value to search on. An input of the empty string should return
     * an empty set of results.
     * @param arguments A set of additional arguments that can be used by the implementor to provide
     * more relevant search results. See `MediaSearchArguments` for more information about which
     * fields are provided and how they are expected to be used. By default, an empty set of
     * arguments is provided.
     *
     * @return A set of `MediaSearchResults` with media that matches the provided [query] and is
     * optionally refined further using the provided [arguments].
     *
     * @see MediaSearchResults
     * @see MediaSearchArguments
     */
    public suspend fun searchForMediaItems(
        query: String,
        arguments: MediaSearchArguments = MediaSearchArguments()
    ): MediaSearchResults<M>

}
