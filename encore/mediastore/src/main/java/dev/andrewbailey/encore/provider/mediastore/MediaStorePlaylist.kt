package dev.andrewbailey.encore.provider.mediastore

import android.net.Uri
import android.os.Parcelable
import dev.andrewbailey.annotations.compose.ComposeStableClass
import kotlinx.parcelize.Parcelize

@ComposeStableClass
@Parcelize
public data class MediaStorePlaylist(
    val id: String,
    val name: String,
    internal val path: String,
    internal val contentsUri: Uri
) : Parcelable
