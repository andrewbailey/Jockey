package dev.andrewbailey.encore.player.util

import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaControllerCompat
import androidx.test.platform.app.InstrumentationRegistry
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

suspend inline fun <reified S : Service> mediaBrowserFor(
    context: Context = InstrumentationRegistry.getInstrumentation().context,
    serviceContext: Context = InstrumentationRegistry.getInstrumentation().targetContext,
    rootHints: Bundle? = null
): MediaBrowserCompat {
    return suspendCancellableCoroutine { cont ->
        lateinit var browser: MediaBrowserCompat

        browser = MediaBrowserCompat(
            context,
            ComponentName(serviceContext, S::class.java),
            object : MediaBrowserCompat.ConnectionCallback() {
                override fun onConnected() {
                    cont.resume(browser)
                }

                override fun onConnectionFailed() {
                    cont.resumeWithException(RuntimeException("Failed to bind to browser"))
                }
            },
            rootHints
        )

        browser.connect()
        cont.invokeOnCancellation {
            browser.disconnect()
        }
    }
}

fun mediaControllerFrom(
    browser: MediaBrowserCompat,
    context: Context = InstrumentationRegistry.getInstrumentation().context
): MediaControllerCompat {
    return MediaControllerCompat(context, browser.sessionToken)
}
