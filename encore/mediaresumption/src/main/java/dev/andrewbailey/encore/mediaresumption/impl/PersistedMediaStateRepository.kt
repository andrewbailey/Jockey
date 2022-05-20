package dev.andrewbailey.encore.mediaresumption.impl

import android.content.Context
import androidx.room.Room
import androidx.room.withTransaction
import dev.andrewbailey.encore.mediaresumption.impl.db.MediaPersistenceDatabase
import dev.andrewbailey.encore.model.MediaObject
import dev.andrewbailey.encore.player.state.MediaPlaybackState
import dev.andrewbailey.encore.player.state.ShuffleMode.ShuffleDisabled
import dev.andrewbailey.encore.player.state.ShuffleMode.ShuffleEnabled
import dev.andrewbailey.encore.provider.MediaProvider

internal class PersistedMediaStateRepository<M : MediaObject>(
    context: Context,
    mediaProvider: MediaProvider<M>
) {

    private val converter = PersistenceConverter(mediaProvider)
    private val database = Room.databaseBuilder(
        context.applicationContext,
        MediaPersistenceDatabase::class.java,
        "encore-persistence"
    ).build()

    suspend fun saveState(state: MediaPlaybackState<M>) {
        val playbackState = converter.toPersistedPlaybackState(state)
        val queueItems = converter.toPersistedQueueItems(state)

        database.withTransaction {
            if (playbackState == null) {
                database.playbackStateDao().deletePersistedPlaybackState()
            } else {
                database.playbackStateDao().setPersistedPlaybackState(playbackState)
            }

            database.queueDao().setPersistedQueueItems(queueItems)
        }
    }

    suspend fun getState(): MediaPlaybackState<M>? {
        return database.withTransaction {
            database.playbackStateDao().getPersistedPlaybackState()?.let { playbackState ->
                converter.toTransportState(
                    persistedPlaybackState = playbackState,
                    persistedQueueItems = database.queueDao().getPersistedQueueItems()
                )
            }
        }
    }

    suspend fun getLastPlayingItem(): M? {
        val playbackState = database.playbackStateDao().getPersistedPlaybackState()
        val queueItem = when (playbackState?.shuffleMode) {
            ShuffleDisabled -> {
                database.queueDao().getQueueItemAtIndex(playbackState.queueIndex)
            }
            ShuffleEnabled -> {
                database.queueDao().getQueueItemAtShuffledIndex(playbackState.queueIndex)
            }
            null -> null
        }

        return queueItem?.let { converter.toMediaObject(it) }
    }

}
