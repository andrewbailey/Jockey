package dev.andrewbailey.encore.mediaresumption.impl.db

import androidx.room.Database
import androidx.room.RoomDatabase
import dev.andrewbailey.encore.mediaresumption.impl.model.PersistedPlaybackState
import dev.andrewbailey.encore.mediaresumption.impl.model.PersistedQueueItem

@Database(
    entities = [
        PersistedPlaybackState::class,
        PersistedQueueItem::class
    ],
    version = 1
)
internal abstract class MediaPersistenceDatabase : RoomDatabase() {

    abstract fun playbackStateDao(): PersistedPlaybackStateDao

    abstract fun queueDao(): PersistedQueueItemDao

}
