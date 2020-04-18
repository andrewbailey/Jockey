package dev.andrewbailey.encore.player.os

import android.content.Context
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.MediaMetadataCompat.*
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.support.v4.media.session.PlaybackStateCompat.*
import dev.andrewbailey.encore.player.playback.PlaybackExtension
import dev.andrewbailey.encore.player.state.MediaPlayerState
import dev.andrewbailey.encore.player.state.MediaPlayerState.Prepared
import dev.andrewbailey.encore.player.state.MediaPlayerState.Ready
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

    override fun onNewPlayerState(newState: MediaPlayerState) {
        mediaSession.apply {
            setRepeatMode(when (newState.transportState.repeatMode) {
                REPEAT_NONE -> REPEAT_MODE_NONE
                REPEAT_ONE -> REPEAT_MODE_ONE
                REPEAT_ALL -> REPEAT_MODE_ALL
            })

            when (newState) {
                is Prepared -> {
                    setMetadata(buildMetadata(newState))

                    setPlaybackState(PlaybackStateCompat.Builder()
                        .setState(
                            when (newState.transportState.status) {
                                PlaybackState.PLAYING -> STATE_PLAYING
                                PlaybackState.PAUSED, PlaybackState.REACHED_END -> STATE_PAUSED
                            },
                            newState.transportState.seekPosition.seekPositionMillis,
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
                is Ready -> {
                    setPlaybackState(PlaybackStateCompat.Builder()
                        .setState(STATE_NONE, 0, 0f)
                        .build())
                    setMetadata(MediaMetadataCompat.Builder()
                        .putString(METADATA_KEY_TITLE, "Nothing is playing")
                        .build())
                }
            }
        }
    }

    private fun buildMetadata(state: Prepared): MediaMetadataCompat {
        val nowPlaying = state.transportState.queue.nowPlaying
        return MediaMetadataCompat.Builder()
            .putString(METADATA_KEY_TITLE, nowPlaying.mediaItem.name)
            .putString(METADATA_KEY_AUTHOR, nowPlaying.mediaItem.author?.name)
            .putString(METADATA_KEY_ALBUM, nowPlaying.mediaItem.collection?.name)
            .putLong(METADATA_KEY_DURATION, state.durationMs ?: -1)
            .putBitmap(METADATA_KEY_ALBUM_ART, state.artwork)
            .build()
    }

    override fun onRelease() {
        mediaSession.release()
    }

    private inner class MediaSessionCallback : MediaSessionCompat.Callback() {
        override fun onPlay() {
            modifyTransportState { play() }
        }

        override fun onPause() {
            modifyTransportState { pause() }
        }

        override fun onStop() {
            modifyTransportState { seekTo(0).pause() }
        }

        override fun onSeekTo(pos: Long) {
            modifyTransportState { seekTo(pos) }
        }

        override fun onSkipToPrevious() {
            modifyTransportState { skipToPrevious() }
        }

        override fun onSkipToNext() {
            modifyTransportState { skipToNext() }
        }

        override fun onSetRepeatMode(repeatMode: Int) {
            modifyTransportState {
                setRepeatMode(
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
            modifyTransportState {
                setShuffleMode(
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
