package dev.andrewbailey.encore.model

import android.os.Parcelable
import java.util.UUID
import kotlinx.android.parcel.Parcelize

@Parcelize
public data class QueueItem(
    val queueId: UUID,
    val mediaItem: MediaItem
) : Parcelable
