package dev.andrewbailey.encore.player.util

import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first

@Suppress("EXPERIMENTAL_API_USAGE")
internal class Resource<T : Any> {

    private val reference = ConflatedBroadcastChannel<T?>()

    fun setResource(resource: T) {
        check(reference.offer(resource)) {
            "Failed to send updated resource value"
        }
    }

    fun clearResource() {
        check(reference.offer(null)) {
            "Failed to clear updated resource value"
        }
    }

    fun flow(): Flow<T?> = reference.openSubscription().consumeAsFlow()

    suspend inline fun getResource(
        crossinline filter: (T) -> Boolean = { true }
    ): T {
        return flow()
            .filterNotNull()
            .first { filter(it) }
    }

}
