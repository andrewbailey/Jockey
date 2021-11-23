package dev.andrewbailey.encore.player.browse.verification

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi

internal sealed class BrowserClientPackageResolver {

    abstract fun getPackageMetadata(packageName: String): BrowserClientPackageMetadata?

    companion object {
        fun getInstance(context: Context): BrowserClientPackageResolver {
            val packageManager = context.applicationContext.packageManager
            return if (Build.VERSION.SDK_INT >= 28) {
                BrowserClientPackageResolverApi28(packageManager)
            } else {
                BrowserClientPackageResolverBase(packageManager)
            }
        }
    }
}

private class BrowserClientPackageResolverBase(
    private val packageManager: PackageManager
) : BrowserClientPackageResolver() {

    @Suppress("DEPRECATION")
    @SuppressLint("PackageManagerGetSignatures")
    override fun getPackageMetadata(packageName: String): BrowserClientPackageMetadata? {
        val packageInfo: PackageInfo? = packageManager.getPackageInfo(
            packageName,
            PackageManager.GET_SIGNATURES or PackageManager.GET_PERMISSIONS
        )

        val signature = packageInfo?.signatures?.takeIf { it.size == 1 }?.firstOrNull()?.toHash()

        return if (packageInfo == null || signature == null) {
            null
        } else {
            BrowserClientPackageMetadata(
                signatures = listOf(signature),
                permissions = packageInfo.getGrantedPermissions()
            )
        }
    }

}

@RequiresApi(28)
private class BrowserClientPackageResolverApi28(
    private val packageManager: PackageManager
) : BrowserClientPackageResolver() {

    override fun getPackageMetadata(packageName: String): BrowserClientPackageMetadata? {
        val packageInfo = packageManager.getPackageInfo(
            packageName,
            PackageManager.GET_SIGNING_CERTIFICATES or PackageManager.GET_PERMISSIONS
        )

        val signatures = packageInfo?.signingInfo?.signingCertificateHistory?.map { it.toHash() }

        return if (packageInfo == null || signatures == null) {
            null
        } else {
            BrowserClientPackageMetadata(
                signatures = signatures,
                permissions = packageInfo.getGrantedPermissions()
            )
        }
    }

}

private fun PackageInfo.getGrantedPermissions(): Set<String> {
    return requestedPermissions?.zip(requestedPermissionsFlags.asIterable())
        .orEmpty()
        .filter { (_, flag) -> flag and PackageInfo.REQUESTED_PERMISSION_GRANTED != 0 }
        .map { (packageName, _) -> packageName }
        .toSet()
}
