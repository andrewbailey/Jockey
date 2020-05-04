package dev.andrewbailey.encore.player.browse.verification

import android.Manifest.permission.BIND_NOTIFICATION_LISTENER_SERVICE
import android.Manifest.permission.MEDIA_CONTENT_CONTROL
import android.content.Context
import android.os.Process

internal class BrowserPackageValidator(
    context: Context
) {

    private val packageResolver = BrowserClientPackageResolver.getInstance(context)
    private val checkedClients = mutableMapOf<BrowserClient, Boolean>()

    private val platformSignature: String
    private val mySignatures: List<String>

    init {
        val platformSignatures = packageResolver.getPackageMetadata(ANDROID_PLATFORM)?.signatures
        platformSignature = when {
            platformSignatures == null -> {
                throw RuntimeException("Failed to resolve Android platform")
            }
            platformSignatures.isEmpty() -> {
                throw RuntimeException("Android platform has no signatures")
            }
            platformSignatures.size > 1 -> {
                throw RuntimeException("Android platform has multiple signatures")
            }
            else -> platformSignatures.first()
        }

        mySignatures = packageResolver.getPackageMetadata(context.packageName)?.signatures.orEmpty()
    }

    fun isClientAllowedToBind(client: BrowserClient): Boolean {
        return checkedClients.getOrPut(client) {
            doesClientHaveBindPermission(client)
        }
    }

    private fun doesClientHaveBindPermission(client: BrowserClient): Boolean {
        return when {
            client.uid == Process.myUid() -> true
            client.uid == Process.SYSTEM_UID -> true
            doesPackageHaveBindPermission(client.packageName) -> true
            else -> false
        }
    }

    private fun doesPackageHaveBindPermission(packageName: String): Boolean {
        val packageMetadata = packageResolver.getPackageMetadata(packageName)
        return when {
            packageMetadata == null -> false
            platformSignature in packageMetadata.signatures -> true
            mySignatures.any { it in packageMetadata.signatures } -> true
            isWhitelisted(packageName, packageMetadata.signatures) -> true
            packageMetadata.permissions.contains(MEDIA_CONTENT_CONTROL) -> true
            packageMetadata.permissions.contains(BIND_NOTIFICATION_LISTENER_SERVICE) -> true
            else -> false
        }
    }

    private fun isWhitelisted(packageName: String, signatures: Collection<String>): Boolean {
        return signatures.any { it in whitelistedPackages[packageName].orEmpty() }
    }

    companion object {
        private const val ANDROID_PLATFORM = "android"

        @Suppress("SpellCheckingInspection")
        private val whitelistedPackages: Map<String, Set<String>> = mapOf(
            // Android Auto
            "com.google.android.projection.gearhead" to setOf(
                "fdb00c43dbde8b51cb312aa81d3b5fa17713adb94b28f598d77f8eb89daceedf"
            ),
            // WearOS
            "com.google.android.wearable.app" to setOf(
                "85cd5973541be6f477d847a0bcc6aa2527684b819cd5968529664cb07157b6fe"
            ),
            // Android Auto Simulator
            "com.google.android.autosimulator" to setOf(
                "1975b2f17177bc89a5dff31f9e64a6cae281a53dc1d1d59b1d147fe1c82afa00"
            ),
            // Google Search app
            "com.google.android.googlequicksearchbox" to setOf(
                "f0fd6c5b410f25cb25c3b53346c8972fae30f8ee7411df910480ad6b2d60db83"
            ),
            // Google Assistant on Android Automotive
            "com.google.android.carassistant" to setOf(
                "74b6fbf710e8d90d44d340125889b42306a62c4379d0e5a66220e3a68abf90e2"
            )
        )
    }

}
