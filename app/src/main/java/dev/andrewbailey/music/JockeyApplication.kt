package dev.andrewbailey.music

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import dev.andrewbailey.music.player.PlayerNotifier

class JockeyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationManagerCompat.from(this).createNotificationChannel(NotificationChannel(
                PlayerNotifier.CHANNEL_ID,
                getString(R.string.notification_channel_playback),
                NotificationManager.IMPORTANCE_LOW
            )
            )
        }
    }
}
