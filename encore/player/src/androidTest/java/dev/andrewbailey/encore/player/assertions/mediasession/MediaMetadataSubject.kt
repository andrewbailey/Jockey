package dev.andrewbailey.encore.player.assertions.mediasession

import android.support.v4.media.MediaMetadataCompat
import com.google.common.truth.FailureMetadata
import com.google.common.truth.MapSubject
import com.google.common.truth.Subject
import com.google.common.truth.Truth
import dev.andrewbailey.encore.player.assertions.mediasession.model.MediaMetadataKey
import dev.andrewbailey.encore.player.assertions.mediasession.model.asMap

fun assertThat(mediaMetadataCompat: MediaMetadataCompat?) =
    Truth.assertAbout(MediaMetadataSubject.Factory).that(mediaMetadataCompat)

fun mediaMetadataCompat() = MediaMetadataSubject.Factory

class MediaMetadataSubject private constructor(
    failureMetadata: FailureMetadata,
    private val actual: MediaMetadataCompat?
) : Subject(failureMetadata, actual) {

    companion object {
        val Factory = ::MediaMetadataSubject
    }

    // region assertion operations

    fun hasNoMetadataValues() = hasMetadata(
        expectedMetadata = MediaMetadataKey.values().associateWith { it.defaultValue }
    )

    fun hasMetadata(vararg expectedMetadata: Pair<MediaMetadataKey<Any>, Any?>) {
        hasMetadata(mapOf(*expectedMetadata))
    }

    fun hasMetadata(expectedMetadata: Map<MediaMetadataKey<Any>, Any?>) {
        metadata().containsAtLeastEntriesIn(expectedMetadata)
    }

    // endregion assertion operations

    // region subject accessors

    fun metadata(): MapSubject =
        check("metadata").that(actual?.asMap())

    // endregion subject accessors
}
