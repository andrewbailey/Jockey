package dev.andrewbailey.encore.player.os

/**
 * In release builds, this class only performs no-ops. It exists to match the signature of its
 * debug counterpart so that we can introduce an IdlingResource without shipping it to production.
 */
internal class MediaSessionControllerIdlingResource {

    fun onCompleteCommand() {

    }
}
