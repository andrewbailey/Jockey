package dev.andrewbailey.encore.player.os

import androidx.test.espresso.IdlingResource
import androidx.test.espresso.IdlingResource.ResourceCallback
import kotlinx.atomicfu.atomic

internal class MediaSessionControllerIdlingResource : IdlingResource {

    private var resourceCallback: ResourceCallback? = null

    private var targetCommandCount = 0
    private val commandsCompleted = atomic(0)

    fun onCompleteCommand() {
        commandsCompleted.incrementAndGet()
        if (isIdleNow) {
            resourceCallback?.onTransitionToIdle()
        }
    }

    fun setTargetNumberOfCommands(targetCommandsReceived: Int) {
        targetCommandCount = targetCommandsReceived
    }

    override fun getName() = "MediaSessionController commands"

    override fun isIdleNow(): Boolean = commandsCompleted.value >= targetCommandCount

    override fun registerIdleTransitionCallback(callback: ResourceCallback?) {
        resourceCallback = callback
    }

}
