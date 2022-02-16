package dev.andrewbailey.encore.mediaresumption.impl.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "queue")
internal data class PersistedQueueItem(
    @PrimaryKey
    val queueId: UUID,
    @ColumnInfo(index = true)
    val index: Int,
    @ColumnInfo(index = true)
    val shuffledIndex: Int,
    val mediaItemId: String
)
