package dev.andrewbailey.music.ui.library

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.andrewbailey.encore.provider.mediastore.MediaStoreProvider
import dev.andrewbailey.encore.provider.mediastore.MediaStoreSong
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val mediaProvider: MediaStoreProvider
) : ViewModel() {

    val songs = MutableLiveData<List<MediaStoreSong>>().apply {
        viewModelScope.launch {
            value = mediaProvider.getAllSongs()
        }
    } as LiveData<List<MediaStoreSong>>

}
