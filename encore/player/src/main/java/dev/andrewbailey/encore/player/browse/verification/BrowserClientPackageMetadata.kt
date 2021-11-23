package dev.andrewbailey.encore.player.browse.verification

internal data class BrowserClientPackageMetadata(
    val signatures: List<String>,
    val permissions: Set<String>
)
