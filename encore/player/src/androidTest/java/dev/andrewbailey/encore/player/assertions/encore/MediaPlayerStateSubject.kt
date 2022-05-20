package dev.andrewbailey.encore.player.assertions.encore

import android.graphics.Bitmap
import com.google.common.truth.ClassSubject
import com.google.common.truth.Fact
import com.google.common.truth.Fact.fact
import com.google.common.truth.Fact.simpleFact
import com.google.common.truth.FailureMetadata
import com.google.common.truth.Subject
import com.google.common.truth.Truth.assertAbout
import dev.andrewbailey.encore.player.state.BufferingState
import dev.andrewbailey.encore.player.state.MediaPlayerState
import dev.andrewbailey.encore.player.state.MediaPlayerState.Initializing
import dev.andrewbailey.encore.player.state.MediaPlayerState.Prepared
import dev.andrewbailey.encore.player.state.MediaPlayerState.Ready

fun assertThat(
    mediaPlayerState: MediaPlayerState<*>?
): MediaPlayerStateSubject = assertAbout(mediaPlayerState()).that(mediaPlayerState)

fun mediaPlayerState() = MediaPlayerStateSubject.Factory

class MediaPlayerStateSubject private constructor(
    failureMetadata: FailureMetadata,
    private val actual: MediaPlayerState<*>?
) : Subject(failureMetadata, actual) {

    companion object {
        val Factory = ::MediaPlayerStateSubject
    }

    // region assertion operations

    inline fun <reified T : MediaPlayerState<*>> isInstanceOf() {
        subclass().isAssignableTo(T::class.java)
    }

    fun hasArtwork(bitmap: Bitmap?) {
        val artwork = when (actual) {
            is Prepared -> actual.artwork
            is Ready -> {
                fail(
                    fact("expected", bitmap),
                    simpleFact("but state was Ready (not Prepared)")
                )
            }
            Initializing -> {
                fail(
                    fact("expected", bitmap),
                    simpleFact("but state was Initializing")
                )
            }
            null -> {
                fail(
                    fact("expected", bitmap),
                    simpleFact("but state was null")
                )
            }
        }

        if (bitmap == null) {
            check("artwork").that(artwork).isNull()
        } else if (artwork == null) {
            fail(
                simpleFact("expected a non-null bitmap, but artwork was null")
            )
        } else if (!artwork.sameAs(bitmap)) {
            fail(
                simpleFact("bitmaps were not the same"),
                fact("expected dimensions: ", "(${bitmap.width}, ${bitmap.height})"),
                fact("actual dimensions: ", "(${artwork.width}, ${artwork.height})"),
                fact("expected configuration: ", bitmap.config),
                fact("actual configuration: ", artwork.config),
            )
        }
    }

    fun hasDuration(
        expectedDurationMs: Long,
        thresholdMs: Long = 200
    ) {
        val duration = when (actual) {
            is Prepared -> actual.durationMs
            is Ready -> {
                fail(
                    fact("expected", "$expectedDurationMs ms"),
                    fact("with tolerance of", "$thresholdMs ms"),
                    simpleFact("but state was Ready (not Prepared)")
                )
            }
            Initializing -> {
                fail(
                    fact("expected", "$expectedDurationMs ms"),
                    fact("with tolerance of", "$thresholdMs ms"),
                    simpleFact("but state was Initializing")
                )
            }
            null -> {
                fail(
                    fact("expected", "$expectedDurationMs ms"),
                    fact("with tolerance of", "$thresholdMs ms"),
                    simpleFact("but state was null")
                )
            }
        }

        if (duration == null) {
            fail(
                fact("expected", "$expectedDurationMs ms"),
                fact("with tolerance of", "$thresholdMs ms"),
                simpleFact("but duration was null")
            )
        } else {
            val lowerBound = (expectedDurationMs - thresholdMs).coerceAtLeast(0)
            val upperBound = expectedDurationMs + thresholdMs

            if (duration !in lowerBound..upperBound) {
                fail(
                    fact("expected", "$expectedDurationMs ms"),
                    fact("with tolerance of", "$thresholdMs ms"),
                    fact("but duration was", "$duration ms")
                )
            }
        }
    }

    fun hasExactDuration(
        expectedDurationMs: Long
    ) = hasDuration(expectedDurationMs, thresholdMs = 0)

    fun hasBufferingState(
        expected: BufferingState
    ) {
        val bufferingState = when (actual) {
            is Prepared -> actual.bufferingState
            is Ready -> {
                fail(
                    fact("expected", expected),
                    simpleFact("but state was Ready (not Prepared)")
                )
            }
            Initializing -> {
                fail(
                    fact("expected", expected),
                    simpleFact("but state was Initializing")
                )
            }
            null -> {
                fail(
                    fact("expected", expected),
                    simpleFact("but state was null")
                )
            }
        }

        check("bufferingState")
            .that(bufferingState)
            .isEqualTo(expected)
    }

    // endregion assertion operations

    // region subject accessors

    fun transportState(): TransportStateSubject =
        check("transportState")
            .about(TransportStateSubject.Factory)
            .that(actual?.mediaPlaybackState)

    fun subclass(): ClassSubject =
        check("class").that(actual?.javaClass)

    // endregion subject accessors

    private fun fail(first: Fact?, vararg rest: Fact): Nothing {
        failWithoutActual(first, *rest)
        throw AssertionError()
    }

}
