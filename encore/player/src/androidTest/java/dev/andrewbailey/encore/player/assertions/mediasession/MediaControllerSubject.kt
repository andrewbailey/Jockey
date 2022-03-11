package dev.andrewbailey.encore.player.assertions.mediasession

import android.support.v4.media.session.MediaControllerCompat
import com.google.common.truth.BooleanSubject
import com.google.common.truth.FailureMetadata
import com.google.common.truth.IterableSubject
import com.google.common.truth.Subject
import com.google.common.truth.Truth
import dev.andrewbailey.encore.player.assertions.mediasession.model.MediaDescriptionKey
import dev.andrewbailey.encore.player.assertions.mediasession.model.asMap

fun assertThat(mediaMetadataCompat: MediaControllerCompat?) =
    Truth.assertAbout(MediaControllerSubject.Factory).that(mediaMetadataCompat)

fun mediaControllerCompat() = MediaControllerSubject.Factory

class MediaControllerSubject private constructor(
    failureMetadata: FailureMetadata,
    private val actual: MediaControllerCompat?
) : Subject(failureMetadata, actual) {

    companion object {
        val Factory = ::MediaControllerSubject
    }

    // region assertion operations

    fun sessionIsReady() = isSessionReady().isTrue()

    fun sessionIsNotReady() = isSessionReady().isFalse()

    fun queueExactlyMatches(expectedQueue: List<Pair<Long, Map<MediaDescriptionKey<*>, *>>>) {
        check("queue contents")
            .that(
                actual?.queue?.map { queueItem ->
                    queueItem.queueId to queueItem.description.asMap(includeNullValues = false)
                }
            )
            .containsExactlyElementsIn(expectedQueue)
            .inOrder()
    }

    // endregion assertion operations

    // region subject accessors

    fun isSessionReady(): BooleanSubject =
        check("isSessionReady").that(actual?.isSessionReady)

    fun playbackState(): PlaybackStateSubject =
        check("playbackState").about(playbackStateCompat()).that(actual?.playbackState)

    fun metadata(): MediaMetadataSubject =
        check("playbackState").about(mediaMetadataCompat()).that(actual?.metadata)

    fun queue(): IterableSubject = check("queue").that(actual?.queue)

    // endregion subject accessors
}
