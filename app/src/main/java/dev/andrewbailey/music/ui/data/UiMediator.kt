package dev.andrewbailey.music.ui.data

import dagger.hilt.android.ActivityRetainedLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

open class UiMediator(
    lifecycle: ActivityRetainedLifecycle
) {

    protected val coroutineScope by lazy {
        CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    }

    init {
        lifecycle.addOnClearedListener { destroy() }
    }

    private fun destroy() {
        coroutineScope.cancel()
        onDestroy()
    }

    protected open fun onDestroy() {

    }

}
