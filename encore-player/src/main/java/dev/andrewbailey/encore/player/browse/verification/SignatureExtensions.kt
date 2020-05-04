package dev.andrewbailey.encore.player.browse.verification

import android.content.pm.Signature
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

internal fun Signature.toHash(): String {
    val hashAlgorithm = try {
        MessageDigest.getInstance("SHA256")
    } catch (exception: NoSuchAlgorithmException) {
        throw RuntimeException("Could not find SHA256 has algorithm", exception)
    }

    hashAlgorithm.update(toByteArray())
    return hashAlgorithm.digest().joinToString("") { String.format("%02x", it) }
}
