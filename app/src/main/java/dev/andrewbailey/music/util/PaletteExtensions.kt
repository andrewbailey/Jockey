package dev.andrewbailey.music.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.core.graphics.drawable.toBitmap
import androidx.palette.graphics.Palette
import coil.Coil
import coil.request.ImageRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun rememberPalette(
    coilRequest: Any?,
    cache: PaletteCache? = null
): State<Palette?> {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    return remember(coilRequest) {
        val state = mutableStateOf<Palette?>(null)

        if (cache?.isPaletteCached(coilRequest) == true) {
            state.value = cache.getCachedPalette(coilRequest)
        } else {
            coroutineScope.launch {
                val bitmap = Coil.imageLoader(context)
                    .execute(
                        ImageRequest.Builder(context)
                            .data(coilRequest)
                            .build()
                    )
                    .drawable
                    ?.toBitmap()

                if (bitmap != null) {
                    withContext(Dispatchers.Default) {
                        val palette = Palette.from(bitmap).generate()
                        state.value = palette
                        cache?.rememberPalette(coilRequest, palette)
                    }
                } else {
                    cache?.rememberPalette(coilRequest, null)
                }
            }
        }

        state
    }
}

@Composable
fun rememberPaletteCache(): PaletteCache {
    return remember {
        PaletteCache()
    }
}

class PaletteCache {

    private val cache by mutableStateOf(mutableMapOf<Any?, Palette?>())

    fun isPaletteCached(key: Any?): Boolean {
        return key in cache.keys
    }

    fun getCachedPalette(key: Any?): Palette? {
        return cache[key]
    }

    fun rememberPalette(key: Any?, palette: Palette?) {
        cache[key] = palette
    }

}
