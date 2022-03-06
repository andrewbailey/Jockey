package dev.andrewbailey.encore.player.assertions

import android.support.v4.media.session.PlaybackStateCompat.CustomAction
import com.google.common.truth.Correspondence

val CustomActionCorrespondence = Correspondence.from(
    { first: CustomAction?, second: CustomAction? ->
        when {
            first == null && second == null -> true
            first != null && second == null -> false
            first == null && second != null -> false
            else -> {
                first!!.name == second!!.name &&
                    first.action == second.action &&
                    first.icon == second.icon &&
                    first.extras == second.extras
            }
        }
    },
    "is equivalent to"
)
