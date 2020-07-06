package dev.andrewbailey.music.di

import dev.andrewbailey.music.player.PlayerService
import dev.andrewbailey.music.ui.library.LibraryFragment
import dev.andrewbailey.music.ui.player.NowPlayingFragment

interface JockeyGraph {

    fun inject(service: PlayerService)

    fun inject(fragment: LibraryFragment)
    fun inject(fragment: NowPlayingFragment)

}
