package dev.andrewbailey.encore.model

import dev.andrewbailey.encore.provider.MediaField
import dev.andrewbailey.encore.provider.MediaProvider

/**
 * Specifies additional information for [MediaProvider.searchForMediaItems]. The information
 * encapsulated in this object is meant to provide hints to the provider so that it can return more
 * relevant search results.
 *
 * It is recommended, but not required, that every media provider attempt to use all fields exposed
 * by this object. This may not be feasible depending on what kind of media is being searched.
 *
 * All of the values in this object are optional. By default, none of these additional inputs are
 * specified. In this case, the `MediaProvider`'s search algorithm will fallback to using just the
 * `query` argument. This may result in getting less-relevant search results than desired.
 */
public class MediaSearchArguments(
    /**
     * Indicates which media field the query inputs are most applicable to. Search implementors may
     * use this field change the ranking algorithm to show/play more accurate results first.
     *
     * Put another way, this field can be used to change what type of media the user is expecting.
     *
     * For example, suppose that you are searching for song "Endgame" by Rise Against (which appears
     * on the album "Endgame"). This field can be used to indicate whether you're searching for the
     * song "Endgame" or the album "Endgame". In this case, to specify that you're looking for the
     * song, you'd set this field to [MediaField.Title]. In the same scenario, to specify that
     * you're looking for the album, you'd set this field to [MediaField.Collection].
     *
     * If this field is set to `null`, then no hints are provided to the `MediaProvider` about which
     * search fields or media types are desired, and the `MediaProvider` will make make its own
     * decisions about how to handle the kind of ambiguities described in the previous example.
     */
    public val preferredSearchField: MediaField? = null,
    /**
     * A set of additional query inputs that are associated with specific fields. These values are
     * used to provide additional hints about what specific media item is desired, in case there are
     * many media items that match the query.
     *
     * For example, if you're searching for the song "Hurt", you can supply a [MediaField.Author]
     * value in this map to indicate whether you are looking for the version by Nine Inch Nails or
     * by Johnny Cash.
     */
    public val fields: Map<MediaField, String> = emptyMap()
)
