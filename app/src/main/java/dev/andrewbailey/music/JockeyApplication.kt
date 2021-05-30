package dev.andrewbailey.music

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import coil.Coil
import coil.ImageLoader
import dagger.hilt.android.HiltAndroidApp
import dev.andrewbailey.music.library.ArtworkProvider
import dev.andrewbailey.music.library.install
import dev.andrewbailey.music.player.PlayerNotifier
import javax.inject.Inject

@HiltAndroidApp
class JockeyApplication : Application() {

    @Inject lateinit var artworkProvider: ArtworkProvider

    override fun onCreate() {
        super.onCreate()

        Coil.setImageLoader(::imageLoader)

        if (Build.VERSION.SDK_INT >= 26) {
            NotificationManagerCompat.from(this).createNotificationChannel(
                NotificationChannel(
                    PlayerNotifier.CHANNEL_ID,
                    getString(R.string.notification_channel_playback),
                    NotificationManager.IMPORTANCE_LOW
                )
            )
        }
    }

    private fun imageLoader() = ImageLoader.Builder(this)
        .componentRegistry {
            install(artworkProvider)
        }
        .build()
}
