package dev.andrewbailey.encore.player.assertions.encore

import com.google.common.truth.ClassSubject
import com.google.common.truth.Fact
import com.google.common.truth.Fact.fact
import com.google.common.truth.Fact.simpleFact
import com.google.common.truth.FailureMetadata
import com.google.common.truth.Subject
import com.google.common.truth.Truth
import dev.andrewbailey.encore.player.state.PlaybackState
import dev.andrewbailey.encore.player.state.QueueState
import dev.andrewbailey.encore.player.state.TransportState

fun assertThat(
    mediaPlayerState: TransportState<*>?
): TransportStateSubject = Truth.assertAbout(transportState()).that(mediaPlayerState)

fun transportState() = TransportStateSubject.Factory

class TransportStateSubject private constructor(
    failureMetadata: FailureMetadata,
    private val actual: TransportState<*>?
) : Subject(failureMetadata, actual) {

    companion object {
        val Factory = ::TransportStateSubject
    }

    // region assertion operations

    inline fun <reified T : TransportState<*>> isInstanceOf() {
        subclass().isAssignableTo(T::class.java)
    }

    fun hasStatus(expected: PlaybackState) {
        val actualStatus = when (actual) {
            is TransportState.Active -> actual.status
            is TransportState.Idle -> {
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
        thresholdMs: Long = 500
    ) {
        val seekPosition = when (actual) {
            is TransportState.Active -> actual.seekPosition.seekPositionMillis
            is TransportState.Idle -> {
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
            is TransportState.Active -> actual.queue
            is TransportState.Idle -> {
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
