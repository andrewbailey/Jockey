package dev.andrewbailey.encore.player.util

import android.app.ActivityManager
import android.app.Service
import android.os.Looper
import androidx.core.content.getSystemService
import androidx.test.platform.app.InstrumentationRegistry

fun waitForServiceToStop(serviceClass: Class<out Service>) {
    check(Looper.myLooper() != Looper.getMainLooper()) {
        "waitForServiceToStop cannot be called from the main thread. This function blocks until" +
            "a service stops. When called from the main thread, the service cannot receive " +
            "lifecycle callbacks if it is running in the same process."
    }

    val startTime = System.currentTimeMillis()
    while (isServiceRunning(serviceClass)) {
        val dT = System.currentTimeMillis() - startTime
        if (dT > 5000) {
            throw IllegalStateException("Service $serviceClass did not stop after 5 seconds")
        }
        Thread.sleep(100)
    }
}

fun isServiceRunning(serviceClass: Class<out Service>): Boolean {
    val testContext = InstrumentationRegistry.getInstrumentation().context
    val activityManager = testContext.getSystemService<ActivityManager>()!!
    // getRunningServices won't return third-party services, but will still return services in
    // the same package -- which is good enough for our tests.
    @Suppress("DEPRECATION")
    return activityManager.getRunningServices(Integer.MAX_VALUE)
        .any { it.service.className == serviceClass.name }
}
