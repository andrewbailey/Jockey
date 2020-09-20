package dev.andrewbailey.music.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import dev.andrewbailey.music.R
import dev.andrewbailey.music.ui.player.NowPlayingViewModel

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel: NowPlayingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.pause()
        setContentView(R.layout.activity_main)
    }

}
