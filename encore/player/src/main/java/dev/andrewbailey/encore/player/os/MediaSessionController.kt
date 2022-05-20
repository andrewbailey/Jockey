package dev.andrewbailey.encore.player.os

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore.Audio.Albums
import android.provider.MediaStore.Audio.Artists
import android.provider.MediaStore.Audio.Genres
import android.provider.MediaStore.Audio.Media
import android.provider.MediaStore.EXTRA_MEDIA_ALBUM
import android.provider.MediaStore.EXTRA_MEDIA_ARTIST
import android.provider.MediaStore.EXTRA_MEDIA_FOCUS
import android.provider.MediaStore.EXTRA_MEDIA_GENRE
import android.provider.MediaStore.EXTRA_MEDIA_TITLE
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.support.v4.media.session.PlaybackStateCompat.ACTION_PAUSE
import android.support.v4.media.session.PlaybackStateCompat.ACTION_PLAY
import android.support.v4.media.session.PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID
import android.support.v4.media.session.PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH
import android.support.v4.media.session.PlaybackStateCompat.ACTION_PLAY_PAUSE
import android.support.v4.media.session.PlaybackStateCompat.ACTION_SEEK_TO
import android.support.v4.media.session.PlaybackStateCompat.ACTION_SET_REPEAT_MODE
import android.support.v4.media.session.PlaybackStateCompat.ACTION_SET_SHUFFLE_MODE
import android.support.v4.media.session.PlaybackStateCompat.ACTION_SKIP_TO_NEXT
import android.support.v4.media.session.PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
import android.support.v4.media.session.PlaybackStateCompat.ACTION_STOP
import android.support.v4.media.session.PlaybackStateCompat.REPEAT_MODE_ALL
import android.support.v4.media.session.PlaybackStateCompat.REPEAT_MODE_GROUP
import android.support.v4.media.session.PlaybackStateCompat.REPEAT_MODE_NONE
import android.support.v4.media.session.PlaybackStateCompat.REPEAT_MODE_ONE
import android.support.v4.media.session.PlaybackStateCompat.SHUFFLE_MODE_ALL
import android.support.v4.media.session.PlaybackStateCompat.SHUFFLE_MODE_GROUP
import android.support.v4.media.session.PlaybackStateCompat.SHUFFLE_MODE_NONE
import android.support.v4.media.session.PlaybackStateCompat.STATE_NONE
import android.support.v4.media.session.PlaybackStateCompat.STATE_PAUSED
import android.support.v4.media.session.PlaybackStateCompat.STATE_PLAYING
import androidx.core.content.IntentCompat.EXTRA_START_PLAYBACK
import dev.andrewbailey.encore.model.MediaObject
import dev.andrewbailey.encore.model.MediaSearchArguments
import dev.andrewbailey.encore.player.browse.BrowserHierarchy
import dev.andrewbailey.encore.player.browse.impl.MediaBrowserImpl
import dev.andrewbailey.encore.player.playback.PlaybackExtension
import dev.andrewbailey.encore.player.state.MediaPlayerState
import dev.andrewbailey.encore.player.state.MediaPlayerState.Initialized
import dev.andrewbailey.encore.player.state.MediaPlayerState.Initializing
import dev.andrewbailey.encore.player.state.MediaPlayerState.Prepared
import dev.andrewbailey.encore.player.state.MediaPlayerState.Ready
import dev.andrewbailey.encore.player.state.PlaybackStatus
import dev.andrewbailey.encore.player.state.RepeatMode.RepeatAll
import dev.andrewbailey.encore.player.state.RepeatMode.RepeatNone
import dev.andrewbailey.encore.player.state.RepeatMode.RepeatOne
import dev.andrewbailey.encore.player.state.ShuffleMode.ShuffleDisabled
import dev.andrewbailey.encore.player.state.ShuffleMode.ShuffleEnabled
import dev.andrewbailey.encore.player.state.factory.PlaybackStateFactory
import dev.andrewbailey.encore.provider.MediaField
import dev.andrewbailey.encore.provider.MediaProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

internal class MediaSessionController<M : MediaObject>(
    context: Context,
    tag: String,
    private val playbackStateFactory: PlaybackStateFactory<M>,
    private val browserHierarchy: BrowserHierarchy<M>,
    private val mediaProvider: MediaProvider<M>
) : PlaybackExtension<M>() {

    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    private val metadataMapper = MediaMetadataMapper()
    private val queueMapper = QueueItemMapper()

    private val mediaSessionRequestQueue = Channel<suspend () -> Unit>()
    private val isPlayerFullyInitialized = MutableStateFlow(false)

    val mediaSession = MediaSessionCompat(context, tag)
    val idlingResource = MediaSessionControllerIdlingResource()

    override fun onAttached() {
        mediaSession.apply {
            setCallback(MediaSessionCallback())
            updateMediaSessionState(getCurrentPlaybackState())
            isActive = true
        }

        coroutineScope.launch {
            for (action in mediaSessionRequestQueue) {
                action()
            }
        }
    }

    override fun onPlayerFullyInitialized() {
        isPlayerFullyInitialized.value = true
    }

    override fun onNewPlayerState(newState: Initialized<M>) {
        updateMediaSessionState(newState)
    }

    private fun dispatchAction(
        action: suspend () -> Unit
    ) {
        coroutineScope.launch {
            awaitPlayerInitialization()
            mediaSessionRequestQueue.send(action)
            idlingResource.onCompleteCommand()
        }
    }

    private suspend fun awaitPlayerInitialization() {
        isPlayerFullyInitialized.filter { it }.first()
    }

    private fun updateMediaSessionState(newState: MediaPlayerState<M>) {
        mediaSession.apply {
            if (newState is Initialized) {
                setRepeatMode(
                    when (newState.mediaPlaybackState.repeatMode) {
                        RepeatNone -> REPEAT_MODE_NONE
                        RepeatOne -> REPEAT_MODE_ONE
                        RepeatAll -> REPEAT_MODE_ALL
                    }
                )

                setShuffleMode(
                    when (newState.mediaPlaybackState.shuffleMode) {
                        ShuffleDisabled -> SHUFFLE_MODE_NONE
                        ShuffleEnabled -> SHUFFLE_MODE_ALL
                    }
                )
            }

            when (newState) {
                is Prepared -> {
                    setMetadata(metadataMapper.toMediaMetadataCompat(newState))
                    setQueue(queueMapper.toQueue(newState.mediaPlaybackState.queue))

                    setPlaybackState(
                        PlaybackStateCompat.Builder()
                            .setState(
                                when (newState.mediaPlaybackState.status) {
                                    PlaybackStatus.Playing -> STATE_PLAYING
                                    is PlaybackStatus.Paused -> STATE_PAUSED
                                },
                                newState.mediaPlaybackState.seekPosition.seekPositionMillis,
                                newState.mediaPlaybackState.playbackSpeed
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
                                    ACTION_PLAY_FROM_MEDIA_ID or
                                    ACTION_PLAY_FROM_SEARCH
                            )
                            .build()
                    )
                }
                is Ready, is Initializing -> {
                    setPlaybackState(
                        PlaybackStateCompat.Builder()
                            .setState(STATE_NONE, 0, 0f)
                            .setActions(
                                ACTION_SET_REPEAT_MODE or
                                    ACTION_SET_SHUFFLE_MODE or
                                    ACTION_PLAY_FROM_MEDIA_ID or
                                    ACTION_PLAY_FROM_SEARCH
                            )
                            .build()
                    )
                    setMetadata(MediaMetadataCompat.Builder().build())
                    setQueue(emptyList())
                }
            }
        }
    }

    override fun onRelease() {
        mediaSession.release()
        coroutineScope.cancel()
        mediaSessionRequestQueue.close()
    }

    private inner class MediaSessionCallback : MediaSessionCompat.Callback() {

        override fun onPlay() {
            dispatchAction {
                modifyTransportState { play() }
            }
        }

        override fun onPause() {
            dispatchAction {
                modifyTransportState { pause() }
            }
        }

        override fun onStop() {
            dispatchAction {
                modifyTransportState { seekTo(0).pause() }
            }
        }

        override fun onSeekTo(pos: Long) {
            dispatchAction {
                modifyTransportState { seekTo(pos) }
            }
        }

        override fun onSkipToPrevious() {
            dispatchAction {
                modifyTransportState { skipToPrevious() }
            }
        }

        override fun onSkipToNext() {
            dispatchAction {
                modifyTransportState { skipToNext() }
            }
        }

        override fun onSetRepeatMode(repeatMode: Int) {
            val encoreRepeatMode = when (repeatMode) {
                REPEAT_MODE_NONE -> RepeatNone
                REPEAT_MODE_ONE -> RepeatOne
                REPEAT_MODE_ALL, REPEAT_MODE_GROUP -> RepeatAll
                else -> throw IllegalArgumentException("Invalid repeat mode: $repeatMode")
            }

            dispatchAction {
                modifyTransportState {
                    setRepeatMode(
                        repeatMode = encoreRepeatMode
                    )
                }
            }
        }

        override fun onSetShuffleMode(shuffleMode: Int) {
            val encoreShuffleMode = when (shuffleMode) {
                SHUFFLE_MODE_NONE -> ShuffleDisabled
                SHUFFLE_MODE_ALL, SHUFFLE_MODE_GROUP -> ShuffleEnabled
                else -> throw IllegalArgumentException("Invalid shuffle mode: $shuffleMode")
            }

            dispatchAction {
                modifyTransportState {
                    setShuffleMode(
                        shuffleMode = encoreShuffleMode
                    )
                }
            }
        }

        override fun onPrepareFromSearch(query: String?, extras: Bundle?) {
            super.onPrepareFromSearch(query, extras)
        }

        override fun onPlayFromSearch(query: String?, extras: Bundle?) {
            if (query.isNullOrEmpty()) {
                // Use provided a generic command (e.g. "Play music"). Attempt to resume playback.

                // TODO: It might be beneficial for library consumers to be able to override this
                //       behavior and change the queue that will be played. Consider adding a new
                //       API to support querying for an undefined media set in the future.
                return onPlay()
            }

            dispatchAction {
                val searchArguments = MediaSearchArguments(
                    preferredSearchField = when (extras?.getString(EXTRA_MEDIA_FOCUS)) {
                        Media.ENTRY_CONTENT_TYPE -> MediaField.Title
                        Artists.ENTRY_CONTENT_TYPE -> MediaField.Author
                        Albums.ENTRY_CONTENT_TYPE -> MediaField.Collection
                        Genres.ENTRY_CONTENT_TYPE -> MediaField.Genre
                        else ->
                            if (extras?.containsKey(EXTRA_MEDIA_TITLE) == true) {
                                MediaField.Title
                            } else {
                                null
                            }
                    },
                    fields = MediaField.values()
                        .mapNotNull { field ->
                            when (field) {
                                MediaField.Title -> extras?.getString(EXTRA_MEDIA_TITLE)
                                MediaField.Author -> extras?.getString(EXTRA_MEDIA_ARTIST)
                                MediaField.Collection -> extras?.getString(EXTRA_MEDIA_ALBUM)
                                MediaField.Genre -> {
                                    if (Build.VERSION.SDK_INT >= 21) {
                                        extras?.getString(EXTRA_MEDIA_GENRE)
                                    } else {
                                        null
                                    }
                                }
                            }?.let { field to it }
                        }
                        .toMap()
                )

                setTransportState(
                    playbackStateFactory.playFromSearchResults(
                        state = requireCurrentTransportState(),
                        query = query,
                        beginPlayback = extras?.getBoolean(EXTRA_START_PLAYBACK, true) ?: true,
                        arguments = searchArguments,
                        searchResults = mediaProvider.searchForMediaItems(query, searchArguments)
                    )
                )
            }
        }

        override fun onPlayFromMediaId(mediaId: String, extras: Bundle?) {
            dispatchAction {
                if (mediaId == MediaBrowserImpl.MEDIA_RESUMPTION_TRACK_ID) {
                    modifyTransportState { play() }
                } else {
                    val browserResults = browserHierarchy.getMediaItems(mediaId)

                    setTransportState(
                        playbackStateFactory.playFromMediaBrowser(
                            state = requireCurrentTransportState(),
                            browserId = mediaId,
                            mediaItemId = browserResults.mediaItemId,
                            mediaItems = browserResults.mediaItems
                        )
                    )
                }
            }
        }
    }

}
