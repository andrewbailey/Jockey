package dev.andrewbailey.encore.provider

/**
 * [MediaField] is an enum class that broadly defines which fields Encore can perform
 * searches on out-of-the box. These fields are used with [MediaProvider.searchForMediaItems] to
 * provide hints to the search algorithm about how to provide more relevant results. The exact
 * implications of these values are left up to the implementation of the [MediaProvider], but some
 * examples are given in this class as to how these fields are expected to affect the search
 * behavior.
 *
 * These values are used by Encore automatically. For example, some system-level integrations (such
 * as voice commands) may request searches using these constants. To define your own searches based
 * on fields specific to your media data or with more advanced matching logic, you should create a
 * new function on the [MediaProvider] implementation itself.
 */
public enum class MediaField {
    /**
     * Indicates that a search value that should specifically be compared against the title of the
     * media items. In general, this should consider the title of a given track. For music, this
     * would be a song title. For podcasts, this would be the name of a specific episode.
     */
    Title,

    /**
     * Indicates that a search value should be specifically be compared against the author name of
     * the media items. The meaning of the author depends on the type of media being searched over.
     *
     * For music, the author is likely the artist or band name. For podcasts, this would probably be
     * the creator or host name. For audiobooks, this would probably be the author of the book.
     */
    Author,

    /**
     * Indicates that a search value should be compared against the name of a collection that the
     * media item is published or distributed in. The exact meaning of what the collection
     * represents depends on the type of media being searched over, but this generally refers to
     * albums.
     *
     * For music, the collection should be the album that a song appears on. Songs that appear
     * independently of an album may be considered singles for the sake of the query. For audiobooks
     * that are part of a series (such as a trilogy), the search might be interpreted as searching
     * for books that are part of the series.
     *
     * Many forms of media (including podcasts) don't have the concept of a collection, and might
     * ignore search values associated with this attribute.
     */
    Collection,

    /**
     * Indicates that a search value should be compared against the name of the genre that the media
     * item is classified as.
     *
     * For music, the genre would represent the style of music. The specificity of the genres will
     * depend on the quality of the metadata. "Rock", "EDM", "Pop", and "Old-World Electronic
     * Post-Rock" might all be valid music genres to search over. For podcasts, you might consider
     * "Comedy", "News", "Programming", and "Design" as possible genres. For audiobooks, you might
     * consider "Fiction", "Non-Fiction", and "Biography" as possible genres.
     */
    Genre
}
