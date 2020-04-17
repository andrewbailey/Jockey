package dev.andrewbailey.music.player

import dev.andrewbailey.encore.player.MediaPlayerService

class PlayerService : MediaPlayerService(
    tag = "Jockey",
    notificationId = 1,
    notificationProvider = PlayerNotifier()
)
