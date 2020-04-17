package dev.andrewbailey.music.ui

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

open class BaseViewModel : ViewModel() {

    private val lifecycleScope = CoroutineScope(Dispatchers.Main)

    fun launch(coroutine: suspend CoroutineScope.() -> Unit) {
        lifecycleScope.launch(block = coroutine)
    }

    override fun onCleared() {
        lifecycleScope.cancel("ViewModel.onCleared was called")
    }

}
