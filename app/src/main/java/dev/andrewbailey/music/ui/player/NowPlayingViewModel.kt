package dev.andrewbailey.music.ui.player

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dev.andrewbailey.encore.player.controller.EncoreController
import dev.andrewbailey.encore.player.controller.EncoreController.SeekUpdateFrequency.WhilePlayingEvery
import dev.andrewbailey.encore.player.state.*
import dev.andrewbailey.encore.provider.mediastore.LocalSong
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.launch

class NowPlayingViewModel @ViewModelInject constructor(
    private val mediaController: EncoreController<LocalSong>
) : ViewModel() {

    private val token = mediaController.acquireToken()

    val playbackState = mediaController
        .observeState(seekUpdateFrequency = WhilePlayingEvery(100, TimeUnit.MILLISECONDS))
        .asLiveData()

    override fun onCleared() {
        mediaController.releaseToken(token)
    }

    fun seekTo(positionMs: Long) {
        mediaController.seekTo(positionMs)
    }

    fun play() {
        mediaController.play()
    }

    fun pause() {
        mediaController.pause()
    }

    fun skipPrevious() {
        mediaController.skipPrevious()
    }

    fun skipNext() {
        mediaController.skipNext()
    }

    fun setShuffleMode(shuffleMode: ShuffleMode) {
        mediaController.setShuffleMode(shuffleMode)
    }

    fun playAtQueueIndex(index: Int) {
        viewModelScope.launch {
            val currentState = mediaController.getState().transportState
            if (currentState is TransportState.Active) {
                mediaController.setState(currentState.copy(
                    status = PlaybackState.PLAYING,
                    seekPosition = SeekPosition.AbsoluteSeekPosition(0L),
                    queue = currentState.queue.copy(queueIndex = index)
                ))
            } else {
                throw IllegalStateException("Cannot change the seek position " +
                        "because nothing is playing.")
            }
        }
    }

}
