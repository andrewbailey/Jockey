package dev.andrewbailey.music.ui.library

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dev.andrewbailey.encore.model.MediaItem
import dev.andrewbailey.encore.player.controller.EncoreController
import dev.andrewbailey.encore.provider.MediaProvider
import dev.andrewbailey.music.ui.BaseViewModel
import javax.inject.Inject

class LibraryViewModel @Inject constructor(
    private val mediaProvider: MediaProvider,
    val mediaController: EncoreController
) : BaseViewModel() {

    private val token = mediaController.acquireToken()

    val songs = MutableLiveData<List<MediaItem>>().apply {
        launch {
            value = mediaProvider.getAllMedia()
        }
    } as LiveData<List<MediaItem>>

    override fun onCleared() {
        mediaController.releaseToken(token)
    }

}
