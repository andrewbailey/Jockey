package dev.andrewbailey.music.ui.data

import androidx.compose.runtime.compositionLocalOf
import dagger.hilt.android.ActivityRetainedLifecycle
import dagger.hilt.android.scopes.ActivityRetainedScoped
import dev.andrewbailey.music.library.MediaRepository
import dev.andrewbailey.music.model.Album
import dev.andrewbailey.music.model.Artist
import dev.andrewbailey.music.model.Song
import javax.inject.Inject
import kotlin.experimental.ExperimentalTypeInference
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn

val LocalMediaLibrary = compositionLocalOf<MediaLibrary> {
    error("No media library has been set")
}

@ActivityRetainedScoped
class MediaLibrary @Inject constructor(
    lifecycle: ActivityRetainedLifecycle,
    private val mediaRepository: MediaRepository
) : UiMediator(lifecycle) {

    val songs: StateFlow<List<Song>?> by lazy {
        flowOfLibraryData {
            emit(mediaRepository.getAllSongs())
        }
    }

    val albums: StateFlow<List<Album>?> by lazy {
        flowOfLibraryData {
            emit(mediaRepository.getAllAlbums())
        }
    }

    val artists: StateFlow<List<Artist>?> by lazy {
        flowOfLibraryData {
            emit(mediaRepository.getAllArtists())
        }
    }

    fun getSongsInAlbum(album: Album): StateFlow<List<Song>?> {
        return flowOfLibraryData {
            emit(mediaRepository.getSongsInAlbum(album))
        }
    }

    fun getSongsByArtist(artist: Artist): StateFlow<List<Song>?> {
        return flowOfLibraryData {
            emit(mediaRepository.getSongsByArtist(artist))
        }
    }

    fun getAlbumsByArtist(artist: Artist): StateFlow<List<Album>?> {
        return flowOfLibraryData {
            emit(mediaRepository.getAlbumsByArtist(artist))
        }
    }

    @OptIn(ExperimentalTypeInference::class)
    private fun <T> flowOfLibraryData(
        @BuilderInference source: suspend FlowCollector<T>.() -> Unit
    ): StateFlow<T?> {
        return flow(source)
            .stateIn(coroutineScope, WhileSubscribed(), null)
    }

}
