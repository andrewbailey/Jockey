package dev.andrewbailey.encore.player.assertions

import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import com.google.common.truth.FailureMetadata
import com.google.common.truth.IntegerSubject
import com.google.common.truth.StringSubject
import com.google.common.truth.Subject

fun customAction() = CustomActionSubject.Factory

class CustomActionSubject private constructor(
    failureMetadata: FailureMetadata,
    private val actual: PlaybackStateCompat.CustomAction?
) : Subject(failureMetadata, actual) {

    companion object {
        val Factory = ::CustomActionSubject
    }

    // region assertion operations

    fun hasAction(expectedAction: String) = action().isEqualTo(expectedAction)

    fun hasName(expectedName: String) = name().isEqualTo(expectedName)

    fun hasIcon(expectedIcon: Int) = icon().isEqualTo(expectedIcon)

    fun hasExtras(expectedExtras: Bundle) = extras().isEqualTo(expectedExtras)

    // endregion assertion operations

    // region subject accessors

    fun action(): StringSubject = check("action").that(actual?.action)

    fun name(): StringSubject = check("name").that(actual?.name?.toString())

    fun icon(): IntegerSubject = check("icon").that(actual?.icon)

    fun extras(): Subject = check("extras").that(actual?.extras)

    // endregion subject accessors

}
