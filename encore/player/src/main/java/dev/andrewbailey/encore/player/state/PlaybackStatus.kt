package dev.andrewbailey.encore.player.state

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

public sealed class PlaybackStatus : Parcelable {

    @Parcelize
    object Playing : PlaybackStatus()

    @Parcelize
    object Paused : PlaybackStatus()

    @Parcelize
    object ReachedEnd : PlaybackStatus()

}
