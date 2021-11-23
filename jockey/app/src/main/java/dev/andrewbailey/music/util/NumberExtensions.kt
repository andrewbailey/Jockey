package dev.andrewbailey.music.util

fun Float.floorMod(other: Int): Float {
    val rem = rem(other)
    return if (rem < 0) rem + other else rem
}
