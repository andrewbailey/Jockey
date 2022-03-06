package dev.andrewbailey.encore.player

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.getSystemService
import dev.andrewbailey.encore.player.browse.BrowserDirectory
import dev.andrewbailey.encore.player.browse.BrowserHierarchy
import dev.andrewbailey.encore.player.notification.NotificationAction
import dev.andrewbailey.encore.player.notification.NotificationProvider
import dev.andrewbailey.encore.player.state.MediaPlayerState
import dev.andrewbailey.encore.provider.MediaProvider
import dev.andrewbailey.encore.test.FakeMusicProvider
import dev.andrewbailey.encore.test.FakeSong

class EncoreTestService : MediaPlayerService<FakeSong>(
    tag = "EncoreTestService",
    notificationId = 1,
    notificationProvider = TestNotificationProvider()
) {

    private val mediaProvider by lazy {
        FakeMusicProvider(this)
    }

    override fun onCreate() {
        if (Build.VERSION.SDK_INT >= 28) {
            getSystemService<NotificationManager>()?.createNotificationChannel(
                NotificationChannel(
                    TestNotificationProvider.NotificationChannelId,
                    "Test player service channel",
                    NotificationManager.IMPORTANCE_LOW
                )
            )
        }

        super.onCreate()
    }

    override fun onCreateMediaProvider(): MediaProvider<FakeSong> {
        return mediaProvider
    }

    override fun isClientAllowedToBrowse(clientPackageName: String, clientUid: Int): Boolean {
        return true
    }

    override fun onCreateMediaBrowserHierarchy(): BrowserHierarchy<FakeSong> {
        return BrowserHierarchy {
            mediaItems("songs") {
                mediaProvider.getAllSongs()
            }

            dynamicPaths(
                identifier = "albums",
                paths = {
                    mediaProvider.getAllAlbums().map {
                        BrowserDirectory.BrowserPath(id = it.id, name = it.name)
                    }
                },
                pathContents = { pathId ->
                    val album = mediaProvider.getAlbumById(pathId)
                    if (album != null) {
                        mediaItems("contents") {
                            mediaProvider.getSongsInAlbum(album)
                        }
                    }
                }
            )

            dynamicPaths(
                identifier = "artists",
                paths = {
                    mediaProvider.getAllArtists().map {
                        BrowserDirectory.BrowserPath(id = it.id, name = it.name)
                    }
                },
                pathContents = { pathId ->
                    val album = mediaProvider.getArtistById(pathId)
                    if (album != null) {
                        mediaItems("contents") {
                            mediaProvider.getSongsByArtist(album)
                        }
                    }
                }
            )
        }
    }

}

private class TestNotificationProvider : NotificationProvider<FakeSong>(
    notificationChannelId = NotificationChannelId
) {
    override fun getNotificationIcon(playbackState: MediaPlayerState<FakeSong>): Int {
        return android.R.drawable.ic_media_play
    }

    override fun getContentIntent(
        context: Context,
        playbackState: MediaPlayerState<FakeSong>
    ): PendingIntent {
        val intent = Intent(context, EncoreTestService::class.java)
        return PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
    }

    override fun getActions(
        playbackState: MediaPlayerState<FakeSong>
    ): List<NotificationAction<FakeSong>> {
        return emptyList()
    }

    companion object {
        const val NotificationChannelId = ""
    }
}
