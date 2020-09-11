package dev.andrewbailey.encore.model

import android.os.Parcelable
import java.util.*
import kotlinx.android.parcel.Parcelize

@Parcelize
public data class QueueItem<out M : MediaItem>(
    val queueId: UUID,
    val mediaItem: M
) : Parcelable
