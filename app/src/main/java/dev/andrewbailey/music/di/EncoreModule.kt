package dev.andrewbailey.music.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dev.andrewbailey.encore.player.controller.EncoreController
import dev.andrewbailey.encore.provider.MediaProvider
import dev.andrewbailey.encore.provider.mediastore.MediaStoreProvider
import dev.andrewbailey.music.player.PlayerService

@Module
class EncoreModule {

    @Provides
    fun provideMediaProvider(context: Context): MediaProvider = MediaStoreProvider(context)

    @Provides
    fun provideEncoreController(context: Context) = EncoreController.create<PlayerService>(context)

}
