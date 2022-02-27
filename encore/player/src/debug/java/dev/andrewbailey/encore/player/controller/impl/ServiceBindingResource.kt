package dev.andrewbailey.encore.player.controller.impl

import androidx.test.espresso.IdlingResource
import androidx.test.espresso.IdlingResource.ResourceCallback

internal class ServiceBindingResource(
    currentState: BindingState,
    desiredState: BindingState
) : IdlingResource {

    private var resourceCallback: ResourceCallback? = null

    var currentState: BindingState = currentState
        set(value) {
            field = value
            checkCurrentlyIdle()
        }

    var desiredState: BindingState = desiredState
        set(value) {
            field = value
            checkCurrentlyIdle()
        }

    override fun getName() = "Encore service connection"

    override fun isIdleNow() = (currentState == desiredState)

    override fun registerIdleTransitionCallback(callback: ResourceCallback?) {
        resourceCallback = callback
    }

    private fun checkCurrentlyIdle() {
        if (isIdleNow) {
            resourceCallback?.onTransitionToIdle()
        }
    }

    enum class BindingState {
        Bound,
        NotBound
    }
}
