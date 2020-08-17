package dev.andrewbailey.music.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Recomposer
import androidx.compose.ui.platform.setContent
import androidx.fragment.app.Fragment

abstract class ComposableFragment : Fragment() {

    final override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return FrameLayout(requireContext()).apply {
            setContent(Recomposer.current()) {
                onCompose()
            }
        }
    }

    @Composable
    abstract fun onCompose()

}
