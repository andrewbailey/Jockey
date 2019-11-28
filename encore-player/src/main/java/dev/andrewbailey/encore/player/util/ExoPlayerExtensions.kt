package dev.andrewbailey.encore.player.util

import com.google.android.exoplayer2.Format
import com.google.android.exoplayer2.metadata.Metadata
import com.google.android.exoplayer2.trackselection.TrackSelection
import com.google.android.exoplayer2.trackselection.TrackSelectionArray

internal fun TrackSelectionArray.toList(): List<TrackSelection> {
    return List(length) { get(it) }.filterNotNull()
}

internal fun TrackSelection.getFormats(): List<Format> {
    return List(length()) { getFormat(it) }
}

internal fun Metadata.getEntries(): List<Metadata.Entry> {
    return List(length()) { get(it) }
}
