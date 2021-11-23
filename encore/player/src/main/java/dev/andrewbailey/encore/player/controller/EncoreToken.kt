package dev.andrewbailey.encore.player.controller

import java.util.UUID

public class EncoreToken internal constructor() {

    private val id = UUID.randomUUID()

    override fun equals(other: Any?): Boolean = other is EncoreToken && this.id == other.id

    override fun hashCode(): Int = id.hashCode()

}
