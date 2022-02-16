package dev.andrewbailey.encore.mediaresumption.impl.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import androidx.room.Transaction
import dev.andrewbailey.encore.mediaresumption.impl.model.PersistedQueueItem

@Dao
internal interface PersistedQueueItemDao {

    @Insert(onConflict = REPLACE)
    suspend fun insertPersistedQueueItems(queue: List<PersistedQueueItem>)

    @Query("SELECT * FROM queue WHERE `index` = :index")
    suspend fun getQueueItemAtIndex(index: Int): PersistedQueueItem?

    @Query("SELECT * FROM queue WHERE shuffledIndex = :index")
    suspend fun getQueueItemAtShuffledIndex(index: Int): PersistedQueueItem?

    @Query("SELECT * FROM queue")
    suspend fun getPersistedQueueItems(): List<PersistedQueueItem>

    @Query("DELETE FROM queue")
    suspend fun deleteAllPersistedQueueItems()

    @Transaction
    suspend fun setPersistedQueueItems(queue: List<PersistedQueueItem>) {
        deleteAllPersistedQueueItems()
        insertPersistedQueueItems(queue)
    }

}
