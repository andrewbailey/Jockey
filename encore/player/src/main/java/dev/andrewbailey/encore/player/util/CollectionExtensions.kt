package dev.andrewbailey.encore.player.util

internal inline fun <T, U> Collection<T>.isUniqueBy(keySelector: (T) -> U): Boolean {
    val items = HashSet<U>()
    forEach {
        val key = keySelector(it)
        if (key in items) {
            return false
        }
        items += key
    }
    return true
}

internal fun <T> Collection<T>.equalsIgnoringOrder(other: Collection<T>): Boolean {
    return this.size == other.size && other.containsAll(this)
}
