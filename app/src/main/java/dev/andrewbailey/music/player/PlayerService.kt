package dev.andrewbailey.music.player

import dev.andrewbailey.encore.player.MediaPlayerService
import dev.andrewbailey.encore.player.browse.BrowserHierarchy

class PlayerService : MediaPlayerService(
    tag = "Jockey",
    notificationId = 1,
    notificationProvider = PlayerNotifier()
) {

    override fun onCreateMediaBrowserHierarchy(): BrowserHierarchy {
        return BrowserHierarchy {
        }
    }

}
