package dev.andrewbailey.encore.player.browse.impl

import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import androidx.media.MediaBrowserServiceCompat
import androidx.media.MediaBrowserServiceCompat.BrowserRoot
import dev.andrewbailey.encore.model.MediaObject
import dev.andrewbailey.encore.player.browse.BrowserHierarchy
import dev.andrewbailey.encore.player.browse.BrowserMediaItem
import dev.andrewbailey.encore.player.browse.MediaResumptionProvider
import dev.andrewbailey.encore.player.browse.verification.BrowserClient
import dev.andrewbailey.encore.player.browse.verification.BrowserPackageValidator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

internal class MediaBrowserImpl<M : MediaObject>(
    private val coroutineScope: CoroutineScope,
    private val hierarchy: BrowserHierarchy<M>,
    private val browserPackageValidator: BrowserPackageValidator,
    private val mediaResumptionProvider: MediaResumptionProvider<M>?
) {

    private val mapper = MediaBrowserMapper()

    fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot {
        return if (isMediaResumptionRequest(clientPackageName, clientUid, rootHints)) {
            BrowserRoot(
                MEDIA_RESUMPTION_ROOT,
                Bundle().apply {
                    putBoolean(BrowserRoot.EXTRA_RECENT, true)
                }
            )
        } else {
            BrowserRoot("/", null)
        }
    }

    fun onLoadChildren(
        parentId: String,
        result: MediaBrowserServiceCompat.Result<List<MediaBrowserCompat.MediaItem>>
    ) {
        result.detach()
        coroutineScope.launch {
            val items = when (parentId) {
                MEDIA_RESUMPTION_ROOT -> getMediaResumptionItems()
                else -> hierarchy.getItems(parentId)
            }

            result.sendResult(items.map { mapper.toMediaBrowserItem(it) })
        }
    }

    private suspend fun getMediaResumptionItems(): List<BrowserMediaItem> {
        return mediaResumptionProvider?.getCurrentTrack()
            ?.let {
                BrowserMediaItem(
                    id = MEDIA_RESUMPTION_TRACK_ID,
                    item = it
                )
            }
            ?.let { listOf(it) }
            .orEmpty()
    }

    private fun isMediaResumptionRequest(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): Boolean {
        return rootHints?.getBoolean(BrowserRoot.EXTRA_RECENT) == true &&
            browserPackageValidator.isSystemClient(
                BrowserClient(
                    packageName = clientPackageName,
                    uid = clientUid
                )
            )
    }

    companion object {
        internal const val MEDIA_RESUMPTION_ROOT = "%MEDIA_RESUMPTION%"
        internal const val MEDIA_RESUMPTION_TRACK_ID = "%MEDIA_RESUMPTION%/0"
    }

}
