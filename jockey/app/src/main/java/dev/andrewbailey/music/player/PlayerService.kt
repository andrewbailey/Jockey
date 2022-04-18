package dev.andrewbailey.music.player

import dagger.hilt.android.AndroidEntryPoint
import dev.andrewbailey.encore.mediaresumption.PlaybackStateSaver
import dev.andrewbailey.encore.player.MediaPlayerService
import dev.andrewbailey.encore.player.browse.BrowserDirectory
import dev.andrewbailey.encore.player.browse.BrowserHierarchy
import dev.andrewbailey.encore.player.browse.MediaResumptionProvider
import dev.andrewbailey.encore.provider.MediaProvider
import dev.andrewbailey.music.R
import dev.andrewbailey.music.library.MediaRepository
import dev.andrewbailey.music.model.Song
import javax.inject.Inject

@AndroidEntryPoint
class PlayerService : MediaPlayerService<Song>(
    tag = "Jockey",
    notificationId = 1,
    notificationProvider = PlayerNotifier()
) {

    @Inject lateinit var mediaRepository: MediaRepository

    override fun onCreateMediaProvider(): MediaProvider<Song> {
        return mediaRepository
    }

    override fun onCreateMediaBrowserHierarchy(): BrowserHierarchy<Song> {
        return BrowserHierarchy {
            staticPath(
                BrowserDirectory.BrowserPath(
                    id = "songs",
                    name = getString(R.string.media_browser_all_songs_section)
                )
            ) {
                mediaItems("song") { mediaRepository.getAllSongs() }
            }
        }
    }

    override fun onCreateMediaResumptionProvider(): MediaResumptionProvider<Song>? {
        return PlaybackStateSaver(this, mediaRepository)
    }

}
