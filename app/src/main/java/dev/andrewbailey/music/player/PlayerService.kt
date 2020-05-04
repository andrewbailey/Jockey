package dev.andrewbailey.music.player

import dev.andrewbailey.encore.player.MediaPlayerService
import dev.andrewbailey.encore.player.browse.BrowserDirectory
import dev.andrewbailey.encore.player.browse.BrowserHierarchy
import dev.andrewbailey.encore.provider.MediaProvider
import dev.andrewbailey.music.JockeyApplication
import dev.andrewbailey.music.R
import javax.inject.Inject

class PlayerService : MediaPlayerService(
    tag = "Jockey",
    notificationId = 1,
    notificationProvider = PlayerNotifier()
) {

    @Inject lateinit var mediaProvider: MediaProvider

    override fun onCreate() {
        JockeyApplication.getComponent(this).inject(this)
        super.onCreate()
    }

    override fun onCreateMediaBrowserHierarchy(): BrowserHierarchy {
        return BrowserHierarchy {
            staticPath(BrowserDirectory.BrowserPath(
                id = "songs",
                name = getString(R.string.media_browser_all_songs_section)
            )) {
                mediaItems("song") { mediaProvider.getAllMedia() }
            }
        }
    }

}
