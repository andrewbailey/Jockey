package dev.andrewbailey.encore.player.os

import android.content.Context
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.MediaMetadataCompat.*
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.support.v4.media.session.PlaybackStateCompat.*
import dev.andrewbailey.encore.player.playback.PlaybackExtension
import dev.andrewbailey.encore.player.state.Active
import dev.andrewbailey.encore.player.state.Idle
import dev.andrewbailey.encore.player.state.PlaybackState
import dev.andrewbailey.encore.player.state.RepeatMode.*
import dev.andrewbailey.encore.player.state.ShuffleMode.LINEAR
import dev.andrewbailey.encore.player.state.ShuffleMode.SHUFFLED
import dev.andrewbailey.encore.player.state.Status

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
        mediaSession.apply {
            when (val transport = newState.transportState) {
                is Active -> {
                    setMetadata(buildMetadata(transport))

                    setPlaybackState(PlaybackStateCompat.Builder()
                        .setState(
                            when (transport.status) {
                                Status.PLAYING -> STATE_PLAYING
                                Status.PAUSED, Status.REACHED_END -> STATE_PAUSED
                            },
                            transport.seekPosition.seekPositionMillis,
                            1.0f
                        )
                        .setActions(
                            ACTION_PLAY or
                            ACTION_PLAY_PAUSE or
                            ACTION_SEEK_TO or
                            ACTION_PAUSE or
                            ACTION_SKIP_TO_NEXT or
                            ACTION_SKIP_TO_PREVIOUS or
                            ACTION_STOP or
                            ACTION_SET_REPEAT_MODE or
                            ACTION_SET_SHUFFLE_MODE or
                            ACTION_PLAY_FROM_MEDIA_ID)
                        .build())
                }
                is Idle -> {
                    setPlaybackState(PlaybackStateCompat.Builder()
                        .setState(STATE_NONE, 0, 0f)
                        .build())
                    setMetadata(MediaMetadataCompat.Builder()
                        .putString(METADATA_KEY_TITLE, "Nothing is playing")
                        .build())
                }
            }
        }

        return super.onNewPlayerState(newState)
    }

    private fun buildMetadata(transport: Active): MediaMetadataCompat {
        return MediaMetadataCompat.Builder()
            .putString(METADATA_KEY_TITLE, transport.nowPlaying.mediaItem.name)
            .putString(METADATA_KEY_AUTHOR, transport.nowPlaying.mediaItem.author?.name)
            .putString(METADATA_KEY_ALBUM, transport.nowPlaying.mediaItem.collection?.name)
            .putBitmap(METADATA_KEY_ALBUM_ART, getArtwork())
            .build()
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
