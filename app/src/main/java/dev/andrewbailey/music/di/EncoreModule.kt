package dev.andrewbailey.music.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.andrewbailey.encore.player.controller.EncoreController
import dev.andrewbailey.encore.provider.mediastore.MediaStoreProvider
import dev.andrewbailey.music.player.PlayerService
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class EncoreModule {

    @Provides
    @Singleton
    fun provideMediaProvider(
        @ApplicationContext context: Context
    ) = MediaStoreProvider(context)

    @Provides
    @Singleton
    fun provideEncoreController(
        @ApplicationContext context: Context
    ) = EncoreController.create(context, PlayerService::class.java)

}
