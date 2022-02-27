package dev.andrewbailey.encore.player.controller.impl

/**
 * In release builds, this class only performs no-ops. It exists to match the signature of its
 * debug counterpart so that we can introduce an IdlingResource without shipping it to production.
 */
internal class ServiceBindingResource(
    @Suppress("UNUSED_PARAMETER") currentState: BindingState,
    @Suppress("UNUSED_PARAMETER") desiredState: BindingState
) {

    var currentState: BindingState
        get() = BindingState.NotBound
        set(_) {}

    var desiredState: BindingState
        get() = BindingState.NotBound
        set(_) {}

    enum class BindingState {
        Bound,
        NotBound
    }
}
