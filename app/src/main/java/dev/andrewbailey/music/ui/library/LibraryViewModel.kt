package dev.andrewbailey.music.ui.library

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dev.andrewbailey.encore.player.controller.EncoreController
import dev.andrewbailey.encore.provider.mediastore.LocalSong
import dev.andrewbailey.encore.provider.mediastore.MediaStoreProvider
import dev.andrewbailey.music.ui.BaseViewModel

class LibraryViewModel @ViewModelInject constructor(
    private val mediaProvider: MediaStoreProvider,
    val mediaController: EncoreController<LocalSong>
) : BaseViewModel() {

    private val token = mediaController.acquireToken()

    val songs = MutableLiveData<List<LocalSong>>().apply {
        launch {
            value = mediaProvider.getAllSongs()
        }
    } as LiveData<List<LocalSong>>

    override fun onCleared() {
        mediaController.releaseToken(token)
    }

}
