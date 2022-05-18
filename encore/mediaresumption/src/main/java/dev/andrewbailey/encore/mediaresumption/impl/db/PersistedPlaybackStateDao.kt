package dev.andrewbailey.encore.mediaresumption.impl.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import dev.andrewbailey.encore.mediaresumption.impl.model.PersistedPlaybackState

@Dao
internal interface PersistedPlaybackStateDao {

    @Query("SELECT * FROM playback_state")
    suspend fun getPersistedPlaybackState(): PersistedPlaybackState?

    @Query("DELETE FROM playback_state")
    suspend fun deletePersistedPlaybackState()

    @Insert(onConflict = REPLACE)
    suspend fun setPersistedPlaybackState(state: PersistedPlaybackState)

}
