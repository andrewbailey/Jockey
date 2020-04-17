package dev.andrewbailey.music.ui.library

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import dev.andrewbailey.music.JockeyApplication
import javax.inject.Inject

class LibraryFragment : Fragment() {

    @Inject
    lateinit var viewModelProvider: ViewModelProvider.Factory

    private val viewModel by viewModels<LibraryViewModel> { viewModelProvider }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        JockeyApplication.getComponent(requireContext()).inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return LibraryContentView(
            context = requireContext(),
            songs = viewModel.songs,
            mediaController = viewModel.mediaController
        )
    }

}
