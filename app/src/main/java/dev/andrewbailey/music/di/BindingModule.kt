package dev.andrewbailey.music.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.andrewbailey.music.library.ArtworkProvider
import dev.andrewbailey.music.library.JockeyArtworkProvider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface BindingModule {

    @Binds
    @Singleton
    fun artworkProvider(artworkProvider: JockeyArtworkProvider): ArtworkProvider

}
