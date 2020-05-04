package dev.andrewbailey.music.di

import dev.andrewbailey.music.player.PlayerService
import dev.andrewbailey.music.ui.library.LibraryFragment

interface JockeyGraph {

    fun inject(service: PlayerService)

    fun inject(fragment: LibraryFragment)

}
