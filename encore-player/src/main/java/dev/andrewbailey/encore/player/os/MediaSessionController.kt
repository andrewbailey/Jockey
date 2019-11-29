package dev.andrewbailey.encore.player.os

import android.content.Context
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat.*
import dev.andrewbailey.encore.player.playback.PlaybackExtension
import dev.andrewbailey.encore.player.state.PlaybackState
import dev.andrewbailey.encore.player.state.RepeatMode.*
import dev.andrewbailey.encore.player.state.ShuffleMode.LINEAR
import dev.andrewbailey.encore.player.state.ShuffleMode.SHUFFLED

internal class MediaSessionController(
    context: Context,
    tag: String
) : PlaybackExtension() {

    val mediaSession = MediaSessionCompat(context, tag)

    override fun onPrepared() {
        mediaSession.apply {
            setCallback(MediaSessionCallback())
            isActive = true
        }
    }

    override fun onNewPlayerState(newState: PlaybackState): PlaybackState {
        mediaSession

        return super.onNewPlayerState(newState)
    }

    override fun onRelease() {
        mediaSession.release()
    }

    private inner class MediaSessionCallback : MediaSessionCompat.Callback() {
        override fun onPlay() {
            modifyPlaybackState { play() }
        }

        override fun onPause() {
            modifyPlaybackState { pause() }
        }

        override fun onStop() {
            modifyPlaybackState { seekTo(0).pause() }
        }

        override fun onSeekTo(pos: Long) {
            modifyPlaybackState { seekTo(pos) }
        }

        override fun onSkipToPrevious() {
            modifyPlaybackState { skipToPrevious() }
        }

        override fun onSkipToNext() {
            modifyPlaybackState { skipToNext() }
        }

        override fun onSetRepeatMode(repeatMode: Int) {
            modifyPlaybackState {
                copy(
                    repeatMode = when (repeatMode) {
                        REPEAT_MODE_NONE -> REPEAT_NONE
                        REPEAT_MODE_ONE -> REPEAT_ONE
                        REPEAT_MODE_ALL, REPEAT_MODE_GROUP -> REPEAT_ALL
                        else -> throw IllegalArgumentException("Invalid repeat mode: $repeatMode")
                    }
                )
            }
        }

        override fun onSetShuffleMode(shuffleMode: Int) {
            modifyPlaybackState {
                copy(
                    shuffleMode = when (shuffleMode) {
                        SHUFFLE_MODE_NONE -> LINEAR
                        SHUFFLE_MODE_ALL, SHUFFLE_MODE_GROUP -> SHUFFLED
                        else -> throw IllegalArgumentException("Invalid shuffle mode: $shuffleMode")
                    }
                )
            }
        }
    }

}
