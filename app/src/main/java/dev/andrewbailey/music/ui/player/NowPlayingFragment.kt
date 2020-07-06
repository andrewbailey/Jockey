package dev.andrewbailey.music.ui.player

import android.os.Bundle
import androidx.compose.Composable
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.ui.material.MaterialTheme
import dev.andrewbailey.music.JockeyApplication
import dev.andrewbailey.music.ui.ComposableFragment
import dev.andrewbailey.music.ui.core.colorPalette
import javax.inject.Inject

class NowPlayingFragment : ComposableFragment() {

    @Inject
    lateinit var viewModelProvider: ViewModelProvider.Factory

    private val viewModel by viewModels<NowPlayingViewModel> { viewModelProvider }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        JockeyApplication.getComponent(requireContext()).inject(this)
    }

    @Composable
    override fun onCompose() {
        MaterialTheme(colorPalette()) {
        }
    }

}
