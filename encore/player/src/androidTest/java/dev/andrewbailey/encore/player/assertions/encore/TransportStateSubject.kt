package dev.andrewbailey.encore.player.assertions.encore

import com.google.common.truth.ClassSubject
import com.google.common.truth.Fact
import com.google.common.truth.Fact.fact
import com.google.common.truth.Fact.simpleFact
import com.google.common.truth.FailureMetadata
import com.google.common.truth.Subject
import com.google.common.truth.Truth
import dev.andrewbailey.encore.player.state.MediaPlaybackState
import dev.andrewbailey.encore.player.state.PlaybackStatus
import dev.andrewbailey.encore.player.state.QueueState

fun assertThat(
    mediaPlayerState: MediaPlaybackState<*>?
): MediaPlaybackStateSubject = Truth.assertAbout(mediaPlaybackState()).that(mediaPlayerState)

fun mediaPlaybackState() = MediaPlaybackStateSubject.Factory

class MediaPlaybackStateSubject private constructor(
    failureMetadata: FailureMetadata,
    private val actual: MediaPlaybackState<*>?
) : Subject(failureMetadata, actual) {

    companion object {
        val Factory = ::MediaPlaybackStateSubject
    }

    // region assertion operations

    inline fun <reified T : MediaPlaybackState<*>> isInstanceOf() {
        subclass().isAssignableTo(T::class.java)
    }

    fun hasStatus(expected: PlaybackStatus) {
        val actualStatus = when (actual) {
            is MediaPlaybackState.Populated -> actual.status
            is MediaPlaybackState.Empty -> {
                fail(
                    fact("expected", expected),
                    simpleFact("but state was not Active")
                )
            }
            null -> {
                fail(
                    fact("expected", expected),
                    simpleFact("but state was null")
                )
            }
        }

        check("status").that(actualStatus).isEqualTo(expected)
    }

    fun hasSeekPosition(
        expectedSeekPositionMs: Long,
        thresholdMs: Long
    ) {
        val seekPosition = when (actual) {
            is MediaPlaybackState.Populated -> actual.seekPosition.seekPositionMillis
            is MediaPlaybackState.Empty -> {
                fail(
                    fact("expected", "$expectedSeekPositionMs ms"),
                    fact("with tolerance of", "$thresholdMs ms"),
                    simpleFact("but state was not Active")
                )
            }
            null -> {
                fail(
                    fact("expected", "$expectedSeekPositionMs ms"),
                    fact("with tolerance of", "$thresholdMs ms"),
                    simpleFact("but state was null")
                )
            }
        }

        val lowerBound = (expectedSeekPositionMs - thresholdMs).coerceAtLeast(0)
        val upperBound = expectedSeekPositionMs + thresholdMs

        if (seekPosition !in lowerBound..upperBound) {
            fail(
                fact("expected", "$expectedSeekPositionMs ms"),
                fact("with tolerance of", "$thresholdMs ms"),
                fact("but seek position was", "$seekPosition ms")
            )
        }
    }

    fun hasExactSeekPosition(
        expectedSeekPositionMs: Long
    ) = hasSeekPosition(expectedSeekPositionMs, thresholdMs = 0)

    fun hasQueueState(expected: QueueState<*>) {
        val actualQueue = when (actual) {
            is MediaPlaybackState.Populated -> actual.queue
            is MediaPlaybackState.Empty -> {
                fail(
                    fact("expected", expected),
                    simpleFact("but state was not Active")
                )
            }
            null -> {
                fail(
                    fact("expected", expected),
                    simpleFact("but state was null")
                )
            }
        }

        check("queue").that(actualQueue).isEqualTo(expected)
    }

    fun isEqualTo(other: Any?, seekToleranceMs: Long) {
        when (actual) {
            null, is MediaPlaybackState.Empty -> {
                isEqualTo(other)
            }
            is MediaPlaybackState.Populated -> {
                val allOtherParametersEqual = other is MediaPlaybackState.Populated<*> &&
                    actual.status == other.status &&
                    actual.queue == other.queue &&
                    actual.repeatMode == other.repeatMode

                if (other !is MediaPlaybackState.Populated<*> || !allOtherParametersEqual) {
                    isEqualTo(other)
                    throw AssertionError()
                }

                val expectedSeekPosition = other.seekPosition.seekPositionMillis
                val actualSeekPosition = actual.seekPosition.seekPositionMillis

                val lowerBound = (expectedSeekPosition - seekToleranceMs).coerceAtLeast(0)
                val upperBound = expectedSeekPosition + seekToleranceMs

                val areSeekPositionsEqual = actualSeekPosition in lowerBound..upperBound

                if (!areSeekPositionsEqual) {
                    fail(
                        simpleFact(
                            "The seek position was not in the desired bounds " +
                                "(All other properties matched the expected values)"
                        ),
                        fact("expected seek position", "$expectedSeekPosition ms"),
                        fact("actual seek position", "$actualSeekPosition ms"),
                        fact("threshold", "$seekToleranceMs ms")
                    )
                }
            }
        }
    }

    // endregion assertion operations

    // region subject accessors

    fun shuffleMode(): Subject =
        check("shuffleMode").that(actual?.shuffleMode)

    fun repeatMode(): Subject =
        check("repeatMode").that(actual?.repeatMode)

    fun subclass(): ClassSubject =
        check("class").that(actual?.javaClass)

    // endregion subject accessors

    private fun fail(first: Fact?, vararg rest: Fact): Nothing {
        failWithoutActual(first, *rest)
        throw AssertionError()
    }
}
