package dev.andrewbailey.encore.model

import android.os.Parcelable

public interface MediaItem : Parcelable {
    public val id: String
    public val playbackUri: String

    public fun toMediaMetadata(): MediaMetadata
}
