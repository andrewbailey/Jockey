package dev.andrewbailey.encore.model

import android.os.Parcelable
import java.util.UUID
import kotlinx.parcelize.Parcelize

@Parcelize
public data class QueueItem<out M : MediaObject>(
    val queueId: UUID,
    val mediaItem: M
) : Parcelable
