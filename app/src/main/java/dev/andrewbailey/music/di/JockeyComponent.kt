package dev.andrewbailey.music.di

import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        ViewModelModule::class,
        ContextModule::class,
        EncoreModule::class
    ]
)
interface JockeyComponent : JockeyGraph
