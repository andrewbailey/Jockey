package dev.andrewbailey.encore.player.os

import android.content.Context
import android.os.Bundle
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.support.v4.media.session.PlaybackStateCompat.*
import dev.andrewbailey.encore.model.MediaObject
import dev.andrewbailey.encore.player.browse.BrowserHierarchy
import dev.andrewbailey.encore.player.playback.PlaybackExtension
import dev.andrewbailey.encore.player.state.MediaPlayerState
import dev.andrewbailey.encore.player.state.MediaPlayerState.Prepared
import dev.andrewbailey.encore.player.state.MediaPlayerState.Ready
import dev.andrewbailey.encore.player.state.PlaybackState
import dev.andrewbailey.encore.player.state.RepeatMode.*
import dev.andrewbailey.encore.player.state.ShuffleMode.LINEAR
import dev.andrewbailey.encore.player.state.ShuffleMode.SHUFFLED
import kotlinx.coroutines.*

internal class MediaSessionController<M : MediaObject>(
    context: Context,
    tag: String,
    private val browserHierarchy: BrowserHierarchy<M>
) : PlaybackExtension<M>() {

    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    private val metadataMapper = MediaMetadataMapper()
    private var mediaSessionActionJob: Job? = null

    val mediaSession = MediaSessionCompat(context, tag)

    override fun onPrepared() {
        mediaSession.apply {
            setCallback(MediaSessionCallback())
            isActive = true
        }
    }

    override fun onNewPlayerState(newState: MediaPlayerState<M>) {
        mediaSession.apply {
            setRepeatMode(
                when (newState.transportState.repeatMode) {
                    REPEAT_NONE -> REPEAT_MODE_NONE
                    REPEAT_ONE -> REPEAT_MODE_ONE
                    REPEAT_ALL -> REPEAT_MODE_ALL
                }
            )

            setShuffleMode(
                when (newState.transportState.shuffleMode) {
                    LINEAR -> SHUFFLE_MODE_NONE
                    SHUFFLED -> SHUFFLE_MODE_ALL
                }
            )

            when (newState) {
                is Prepared -> {
                    setMetadata(metadataMapper.toMediaMetadataCompat(newState))

                    setPlaybackState(
                        PlaybackStateCompat.Builder()
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
                                    ACTION_PLAY_FROM_MEDIA_ID
                            )
                            .build()
                    )
                }
                is Ready -> {
                    setPlaybackState(
                        PlaybackStateCompat.Builder()
                            .setState(STATE_NONE, 0, 0f)
                            .build()
                    )
                    setMetadata(MediaMetadataCompat.Builder().build())
                }
            }
        }
    }

    override fun onRelease() {
        mediaSession.release()
        coroutineScope.cancel()
    }

    private inner class MediaSessionCallback : MediaSessionCompat.Callback() {
        override fun onPlay() {
            onNewAction()
            modifyTransportState { play() }
        }

        override fun onPause() {
            onNewAction()
            modifyTransportState { pause() }
        }

        override fun onStop() {
            onNewAction()
            modifyTransportState { seekTo(0).pause() }
        }

        override fun onSeekTo(pos: Long) {
            onNewAction()
            modifyTransportState { seekTo(pos) }
        }

        override fun onSkipToPrevious() {
            onNewAction()
            modifyTransportState { skipToPrevious() }
        }

        override fun onSkipToNext() {
            onNewAction()
            modifyTransportState { skipToNext() }
        }

        override fun onSetRepeatMode(repeatMode: Int) {
            onNewAction()
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
            onNewAction()
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

        override fun onPlayFromMediaId(mediaId: String, extras: Bundle?) {
            onNewAction()
            mediaSessionActionJob = coroutineScope.launch {
                setTransportState(browserHierarchy.getTransportState(mediaId))
            }
        }

        private fun onNewAction() {
            mediaSessionActionJob?.cancel("Another action has been received")
        }
    }

}
