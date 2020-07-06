package dev.andrewbailey.music.ui.player

import androidx.lifecycle.ViewModel
import dev.andrewbailey.encore.player.controller.EncoreController
import javax.inject.Inject

class NowPlayingViewModel @Inject constructor(
    private val mediaController: EncoreController
) : ViewModel() {

}
