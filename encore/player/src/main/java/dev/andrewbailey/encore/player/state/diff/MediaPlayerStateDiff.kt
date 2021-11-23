package dev.andrewbailey.encore.player.state.diff

import android.os.Parcelable
import dev.andrewbailey.encore.model.MediaObject
import kotlinx.parcelize.Parcelize

@Parcelize
internal class MediaPlayerStateDiff<M : MediaObject>(
    val operations: List<MediaPlayerStateModification<M>>
) : Parcelable
