package dev.andrewbailey.encore.player.action

import android.app.PendingIntent
import android.app.Service
import android.content.Intent

internal object CustomActionIntents {

    private const val ACTION_CUSTOM_ACTION = "com.marverenic.encore.action.CUSTOM_MEDIA_ACTION"
    private const val EXTRA_ACTION_ID = "com.marverenic.encore.extra.ACTION_ID"

    fun createIntent(service: Service, customActionId: String): PendingIntent {
        val intent = Intent(service, service.javaClass).apply {
            action = ACTION_CUSTOM_ACTION
            type = customActionId // Hack to ensure the PendingIntent is unique
            putExtra(EXTRA_ACTION_ID, customActionId)
        }

        return PendingIntent.getService(service, 0, intent, 0)
    }

    fun isCustomActionIntent(intent: Intent): Boolean {
        return intent.action == ACTION_CUSTOM_ACTION
    }

    fun parseActionIdFromIntent(intent: Intent): String? {
        return intent.takeIf { isCustomActionIntent(it) }
            ?.getStringExtra(EXTRA_ACTION_ID)
    }

}
