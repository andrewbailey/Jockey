package dev.andrewbailey.music.ui.library

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.andrewbailey.music.library.MediaRepository
import dev.andrewbailey.music.model.Album
import dev.andrewbailey.music.model.Artist
import dev.andrewbailey.music.model.Song
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val mediaRepository: MediaRepository
) : ViewModel() {

    val songs = MutableLiveData<List<Song>>().apply {
        viewModelScope.launch {
            value = mediaRepository.getAllSongs()
        }
    } as LiveData<List<Song>>

    val albums = MutableLiveData<List<Album>>().apply {
        viewModelScope.launch {
            value = mediaRepository.getAllAlbums()
        }
    } as LiveData<List<Album>>

    val artists = MutableLiveData<List<Artist>>().apply {
        viewModelScope.launch {
            value = mediaRepository.getAllArtists()
        }
    } as LiveData<List<Artist>>

}
