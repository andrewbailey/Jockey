package dev.andrewbailey.encore.model

import android.os.Parcelable

public interface MediaObject : Parcelable {
    public val id: String
    public val playbackUri: String

    public fun toMediaMetadata(): MediaMetadata
}
