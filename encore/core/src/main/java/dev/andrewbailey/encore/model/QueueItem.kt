package dev.andrewbailey.encore.model

import android.os.Parcelable
import dev.andrewbailey.annotations.compose.ComposeStableClass
import java.util.UUID
import kotlinx.parcelize.Parcelize

@ComposeStableClass
@Parcelize
public data class QueueItem<out M : MediaObject>(
    val queueId: UUID,
    val mediaItem: M
) : Parcelable
