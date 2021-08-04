package dev.andrewbailey.encore.player.playback

import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.Timeline
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.TrackSelectionArray

internal class ExoPlayerListeners(
    private val onExoPlayerEvent: () -> Unit
) : Player.Listener {

    override fun onTimelineChanged(timeline: Timeline, reason: Int) {
        onExoPlayerEvent()
    }

    override fun onTracksChanged(
        trackGroups: TrackGroupArray,
        trackSelections: TrackSelectionArray
    ) {
        onExoPlayerEvent()
    }

    override fun onLoadingChanged(isLoading: Boolean) {
        onExoPlayerEvent()
    }

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        onExoPlayerEvent()
    }

    override fun onRepeatModeChanged(repeatMode: Int) {
        onExoPlayerEvent()
    }

    override fun onPositionDiscontinuity(reason: Int) {
        onExoPlayerEvent()
    }

}
