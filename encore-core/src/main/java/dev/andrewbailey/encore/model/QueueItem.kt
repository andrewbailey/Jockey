package dev.andrewbailey.encore.model

import android.os.Parcelable
import java.util.UUID
import kotlinx.android.parcel.Parcelize

@Parcelize
data class QueueItem(
    val queueId: UUID,
    val mediaItem: MediaItem
) : Parcelable
