package dev.andrewbailey.music

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import dev.andrewbailey.music.di.ContextModule
import dev.andrewbailey.music.di.DaggerJockeyComponent
import dev.andrewbailey.music.di.JockeyComponent
import dev.andrewbailey.music.di.JockeyGraph
import dev.andrewbailey.music.player.PlayerNotifier

class JockeyApplication : Application() {

    private lateinit var component: JockeyComponent

    override fun onCreate() {
        super.onCreate()

        if (Build.VERSION.SDK_INT >= 26) {
            NotificationManagerCompat.from(this).createNotificationChannel(NotificationChannel(
                PlayerNotifier.CHANNEL_ID,
                getString(R.string.notification_channel_playback),
                NotificationManager.IMPORTANCE_LOW
            ))
        }

        component = DaggerJockeyComponent.builder()
            .contextModule(ContextModule(this))
            .build()
    }

    companion object {
        fun getInstance(context: Context): JockeyApplication {
            return context.applicationContext as? JockeyApplication
                ?: throw RuntimeException("The application context of $context " +
                        "is not JockeyApplication.")
        }

        fun getComponent(context: Context): JockeyGraph {
            return getInstance(context).component
        }
    }
}
