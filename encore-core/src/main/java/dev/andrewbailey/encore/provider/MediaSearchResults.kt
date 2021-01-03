package dev.andrewbailey.encore.provider

import dev.andrewbailey.encore.model.MediaObject

/**
 * A set of [MediaObject]s that match a given search query. The specifics of how results are
 * generated are unspecified and are implemented independently for every [MediaProvider].
 *
 * @see MediaProvider.searchForMediaItems to perform searches and obtain an instance of this object.
 */
public data class MediaSearchResults<out M : MediaObject>(
    /**
     * A list of media that match part or all of a search query. This list is roughly sorted by the
     * quality of the result, with better matches appearing earlier in the list. If no media matched
     * the query, this list will be empty.
     *
     * All items in the list may be presented directly to the user as search results.
     */
    val searchResults: List<M>,
    /**
     * A list of media that doesn't necessarily match the original query, but is somehow related to
     * the original query. This relationship itself is arbitrary, and depends on both the type of
     * media as well as the provider of the media. The list should roughly be sorted by match
     * quality (if possible), with more related media appearing earlier in the list.
     *
     * This contents in this list aren't intended to be shown directly to a user in the same way
     * that a set of search results is. If the user begins to play media from these results, the
     * continuation can be used to populate the playback queue in a smarter way than just playing
     * all media that matched the query. The big idea here is that if a user searches for a song
     * called "In the End" (or a similarly overloaded title), they probably don't want to
     * exclusively listen to every song called "In the End" in their library.
     *
     * For example, if you're querying for music, the continuation might include other songs in the
     * same genre, on the same album, by the same artist, or some combination of all of these.
     * Alternatively, if you're querying for podcasts, the [searchResults] might contain one episode
     * of a specific podcast, and the continuation might include the subsequent episodes of the
     * podcast, ordered oldest to newest.
     *
     * Items in [searchResults] shouldn't appear in this list. If there isn't any other media that's
     * related to the query (or if all related media is already included in the original search
     * results), this list will be empty. Similarly, if there isn't a useful metric to find similar
     * media or the [MediaProvider] doesn't otherwise support finding related content, this list
     * will also be empty.
     */
    val playbackContinuation: List<M>
)
