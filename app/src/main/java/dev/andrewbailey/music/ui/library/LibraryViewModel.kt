package dev.andrewbailey.music.ui.library

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.andrewbailey.encore.provider.mediastore.MediaStoreProvider
import dev.andrewbailey.encore.provider.mediastore.MediaStoreSong
import kotlinx.coroutines.launch

class LibraryViewModel @ViewModelInject constructor(
    private val mediaProvider: MediaStoreProvider
) : ViewModel() {

    val songs = MutableLiveData<List<MediaStoreSong>>().apply {
        viewModelScope.launch {
            value = mediaProvider.getAllSongs()
        }
    } as LiveData<List<MediaStoreSong>>

}
