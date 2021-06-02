package dev.andrewbailey.music.ui.root

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.andrewbailey.encore.model.QueueItem
import dev.andrewbailey.encore.player.controller.EncoreController
import dev.andrewbailey.encore.player.controller.EncoreController.SeekUpdateFrequency.WhilePlayingEvery
import dev.andrewbailey.encore.player.state.PlaybackState
import dev.andrewbailey.encore.player.state.QueueState
import dev.andrewbailey.encore.player.state.RepeatMode
import dev.andrewbailey.encore.player.state.SeekPosition
import dev.andrewbailey.encore.player.state.ShuffleMode
import dev.andrewbailey.encore.player.state.TransportState
import dev.andrewbailey.encore.player.state.copy
import dev.andrewbailey.music.model.Song
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class PlaybackViewModel @Inject constructor(
    private val mediaController: EncoreController<Song>
) : ViewModel() {

    private val token = mediaController.acquireToken()

    val playbackState = mediaController
        .observeState(seekUpdateFrequency = WhilePlayingEvery(100, TimeUnit.MILLISECONDS))
        .asLiveData()

    override fun onCleared() {
        mediaController.releaseToken(token)
    }

    fun play() {
        mediaController.play()
    }

    fun pause() {
        mediaController.pause()
    }

    fun skipNext() {
        mediaController.skipNext()
    }

    fun skipPrevious() {
        mediaController.skipPrevious()
    }

    fun setShuffleMode(shuffleMode: ShuffleMode) {
        mediaController.setShuffleMode(shuffleMode)
    }

    fun seekTo(positionMs: Long) {
        mediaController.seekTo(positionMs)
    }

    fun playFrom(
        mediaList: List<Song>,
        startingAt: Int = 0
    ) {
        require(mediaList.isNotEmpty()) {
            "Cannot play from an empty list"
        }

        mediaController.setState(
            TransportState.Active(
                status = PlaybackState.PLAYING,
                seekPosition = SeekPosition.AbsoluteSeekPosition(0),
                queue = QueueState.Linear(
                    queue = mediaList.map {
                        QueueItem(
                            queueId = UUID.randomUUID(),
                            mediaItem = it
                        )
                    },
                    queueIndex = startingAt
                ),
                repeatMode = RepeatMode.REPEAT_NONE
            )
        )
    }

    fun playShuffled(
        mediaList: List<Song>
    ) {
        require(mediaList.isNotEmpty()) {
            "Cannot play from an empty list"
        }

        val queueItems = mediaList.map {
            QueueItem(
                queueId = UUID.randomUUID(),
                mediaItem = it
            )
        }

        mediaController.setState(
            TransportState.Active(
                status = PlaybackState.PLAYING,
                seekPosition = SeekPosition.AbsoluteSeekPosition(0),
                queue = QueueState.Shuffled(
                    linearQueue = queueItems,
                    queue = queueItems.shuffled(),
                    queueIndex = 0
                ),
                repeatMode = RepeatMode.REPEAT_NONE
            )
        )
    }

    fun playAtQueueIndex(index: Int) {
        viewModelScope.launch {
            val currentState = mediaController.getState().transportState
            if (currentState is TransportState.Active) {
                mediaController.setState(
                    currentState.copy(
                        status = PlaybackState.PLAYING,
                        seekPosition = SeekPosition.AbsoluteSeekPosition(0L),
                        queue = currentState.queue.copy(queueIndex = index)
                    )
                )
            } else {
                throw IllegalStateException(
                    "Cannot change the seek position " +
                        "because nothing is playing."
                )
            }
        }
    }

}
