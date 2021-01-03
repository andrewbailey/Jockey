package dev.andrewbailey.music.player

import dagger.hilt.android.AndroidEntryPoint
import dev.andrewbailey.encore.player.MediaPlayerService
import dev.andrewbailey.encore.player.browse.BrowserDirectory
import dev.andrewbailey.encore.player.browse.BrowserHierarchy
import dev.andrewbailey.encore.provider.MediaProvider
import dev.andrewbailey.encore.provider.mediastore.MediaStoreProvider
import dev.andrewbailey.encore.provider.mediastore.MediaStoreSong
import dev.andrewbailey.music.R
import javax.inject.Inject

@AndroidEntryPoint
class PlayerService : MediaPlayerService<MediaStoreSong>(
    tag = "Jockey",
    notificationId = 1,
    notificationProvider = PlayerNotifier()
) {

    @Inject lateinit var mediaProvider: MediaStoreProvider

    override fun onCreateMediaProvider(): MediaProvider<MediaStoreSong> {
        return mediaProvider
    }

    override fun onCreateMediaBrowserHierarchy(): BrowserHierarchy<MediaStoreSong> {
        return BrowserHierarchy {
            staticPath(
                BrowserDirectory.BrowserPath(
                    id = "songs",
                    name = getString(R.string.media_browser_all_songs_section)
                )
            ) {
                mediaItems("song") { mediaProvider.getAllSongs() }
            }
        }
    }

}
