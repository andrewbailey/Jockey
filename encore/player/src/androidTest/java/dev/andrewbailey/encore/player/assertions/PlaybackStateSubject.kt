package dev.andrewbailey.encore.player.assertions

import android.support.v4.media.session.PlaybackStateCompat
import android.support.v4.media.session.PlaybackStateCompat.CustomAction
import com.google.common.truth.ComparableSubject
import com.google.common.truth.FailureMetadata
import com.google.common.truth.FloatSubject
import com.google.common.truth.IterableSubject
import com.google.common.truth.LongSubject
import com.google.common.truth.StringSubject
import com.google.common.truth.Subject
import com.google.common.truth.Truth.assertAbout
import dev.andrewbailey.encore.player.assertions.model.PlaybackStateConstants.Action
import dev.andrewbailey.encore.player.assertions.model.PlaybackStateConstants.ErrorCode
import dev.andrewbailey.encore.player.assertions.model.PlaybackStateConstants.State

fun assertThat(playbackStateCompat: PlaybackStateCompat?) =
    assertAbout(PlaybackStateSubject.Factory).that(playbackStateCompat)

fun playbackStateCompat() = PlaybackStateSubject.Factory

class PlaybackStateSubject private constructor(
    failureMetadata: FailureMetadata,
    private val actual: PlaybackStateCompat?
) : Subject(failureMetadata, actual) {

    companion object {
        val Factory = ::PlaybackStateSubject
    }

    // region assertion operations

    fun hasState(expectedState: State) = state().isEqualTo(expectedState)

    fun hasActions(vararg expectedActions: Action, exactly: Boolean = true) {
        if (exactly) {
            actions().containsAtLeastElementsIn(expectedActions)
        } else {
            actions().containsExactlyElementsIn(expectedActions)
        }
    }

    fun hasPosition(
        // lastPositionUpdate time is the elapsed realtime when the seek position was last set on
        // the PlaybackState object.
        expectedPosition: (lastPositionUpdateTime: Long) -> Long
    ) {
        position().isEqualTo(expectedPosition(actual?.lastPositionUpdateTime ?: 0))
    }

    fun hasPlaybackState(expectedSpeed: Float) {
        playbackSpeed().isEqualTo(expectedSpeed)
    }

    // endregion assertion operations

    // region subject accessors

    fun state(): ComparableSubject<State> =
        check("state").that(actual?.state?.let { State.fromFlag(it) })

    fun actions(): IterableSubject =
        check("actions").that(actual?.actions?.let { Action.fromPackedFlags(it) })

    fun customActions(): IterableSubject.UsingCorrespondence<CustomAction?, CustomAction?> =
        check("customActions").that(actual?.customActions)
            .comparingElementsUsing(CustomActionCorrespondence)

    fun position(): LongSubject =
        check("position").that(actual?.position)

    fun bufferedPosition(): LongSubject =
        check("bufferedPosition").that(actual?.bufferedPosition)

    fun playbackSpeed(): FloatSubject =
        check("playbackSpeed").that(actual?.playbackSpeed)

    fun errorCode(): ComparableSubject<ErrorCode> =
        check("errorCode").that(actual?.errorCode?.let { ErrorCode.fromFlag(it) })

    fun errorMessage(): StringSubject =
        check("errorMessage").that(actual?.errorMessage?.toString())

    fun activeQueueItemId(): LongSubject =
        check("activeQueueItemId").that(actual?.activeQueueItemId)

    fun extras(): Subject =
        check("extras").that(actual?.extras)

    // endregion subject accessors
}
