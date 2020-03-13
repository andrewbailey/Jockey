package dev.andrewbailey.encore.player.controller

import java.util.*

class EncoreToken internal constructor() {

    private val id = UUID.randomUUID()

    override fun equals(other: Any?) = other is EncoreToken && this.id == other.id

    override fun hashCode() = id.hashCode()

}
