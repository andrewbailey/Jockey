package dev.andrewbailey.encore.model

import dev.andrewbailey.annotations.compose.ComposeStableClass
import dev.andrewbailey.encore.model.RatingScale.HeartRatingScale
import dev.andrewbailey.encore.model.RatingScale.PercentageRatingScale
import dev.andrewbailey.encore.model.RatingScale.StarRatingScale
import dev.andrewbailey.encore.model.RatingScale.ThumbRatingScale
import dev.andrewbailey.encore.model.RatingValue.HeartRatingValue
import dev.andrewbailey.encore.model.RatingValue.PercentageRatingValue
import dev.andrewbailey.encore.model.RatingValue.StarRatingValue
import dev.andrewbailey.encore.model.RatingValue.ThumbRatingValue

@ComposeStableClass
public data class MediaRating<T : RatingValue>(
    /**
     * The kind of rating scale being used. One of either [HeartRatingScale], [ThumbRatingScale],
     * [StarRatingScale] or [PercentageRatingScale].
     */
    val scale: RatingScale<T>,
    /**
     * The chosen rating value within the bounds of the [scale]. A value of null implies that the
     * media has no rating.
     */
    val value: T?
) {
    init {
        if (value != null) {
            scale.assertValueIsValid(value)
        }
    }
}

@ComposeStableClass
public sealed class RatingScale<T : RatingValue> {

    internal open fun assertValueIsValid(value: T) {
        // Do nothing by default.
    }

    @ComposeStableClass
    public object HeartRatingScale : RatingScale<HeartRatingValue>()

    @ComposeStableClass
    public object ThumbRatingScale : RatingScale<ThumbRatingValue>()

    @ComposeStableClass
    public data class StarRatingScale(
        val maxNumberOfStars: Int
    ) : RatingScale<StarRatingValue>() {
        init {
            require(maxNumberOfStars in 0..5) {
                "`maxNumberOfStars` must be between 0 and 5 (was $maxNumberOfStars)"
            }
        }

        override fun assertValueIsValid(value: StarRatingValue) {
            require(value.numberOfStars in 0..maxNumberOfStars) {
                "The rating's `numberOfStars` must be between 0 and $maxNumberOfStars " +
                    "(was ${value.numberOfStars})"
            }
        }
    }

    @ComposeStableClass
    public object PercentageRatingScale : RatingScale<PercentageRatingValue>()
}

@ComposeStableClass
public sealed class RatingValue {

    @ComposeStableClass
    public sealed class HeartRatingValue : RatingValue() {
        public object Hearted : HeartRatingValue()
        public object NotHearted : HeartRatingValue()
    }

    @ComposeStableClass
    public sealed class ThumbRatingValue : RatingValue() {
        public object ThumbsUp : ThumbRatingValue()
        public object ThumbsDown : ThumbRatingValue()
    }

    @ComposeStableClass
    public data class StarRatingValue(
        val numberOfStars: Int
    ) : RatingValue() {
        init {
            require(numberOfStars >= 0) {
                "`numberOfStars` cannot be negative (was $numberOfStars)"
            }
        }
    }

    @ComposeStableClass
    public data class PercentageRatingValue(
        val percent: Double
    ) : RatingValue() {
        init {
            require(percent in 0.0..100.0) {
                "`percent` must be between 0.0 and 100.0 (was $percent)"
            }
        }
    }

}
