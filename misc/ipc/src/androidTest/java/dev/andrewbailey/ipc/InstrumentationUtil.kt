package dev.andrewbailey.ipc

import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import java.util.concurrent.Semaphore

fun <T> HandlerThread.runBlocking(block: () -> T): T {
    check(Looper.myLooper() != looper) {
        "Cannot synchronously run code on thread $this from the same thread because this will " +
            "instantly cause a permanent deadlock."
    }

    var result: T? = null
    var exception: Throwable? = null
    val semaphore = Semaphore(0)

    Handler(looper).post {
        try {
            result = block()
        } catch (e: Throwable) {
            exception = e
        } finally {
            semaphore.release()
        }
    }

    semaphore.acquire()

    exception?.let { throw it }

    @Suppress("UNCHECKED_CAST")
    return result as T
}

fun HandlerThread.waitForIdleSync() {
    check(Looper.myLooper() != looper) {
        "Cannot synchronously wait for thread $this to become idle because this method has been " +
            "called from the same thread and will instantly cause a permanent deadlock."
    }

    runBlocking { }
}
