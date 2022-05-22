package dev.andrewbailey.music.ui.data

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.core.graphics.drawable.toBitmap
import androidx.palette.graphics.Palette
import coil.ImageLoader
import coil.imageLoader
import coil.request.ImageRequest
import dagger.hilt.android.ActivityRetainedLifecycle
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ActivityRetainedScoped
import dev.andrewbailey.music.model.Album
import javax.inject.Inject
import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.updateAndGet
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async

val LocalColorRepository = compositionLocalOf<ColorRepository> {
    error("No ColorRepository has been set")
}

@ActivityRetainedScoped
class ColorRepository @Inject constructor(
    lifecycle: ActivityRetainedLifecycle,
    @ApplicationContext private val context: Context
) : UiMediator(lifecycle) {

    private val imageLoader: ImageLoader = context.imageLoader

    private val colorRequests = atomic(emptyMap<ColorKey, Deferred<Palette?>>())

    suspend fun paletteOf(album: Album): Palette? {
        return paletteOf(ColorKey.Album(album.id), album)
    }

    private suspend fun paletteOf(key: ColorKey, coilRequest: Any?): Palette? {
        return colorRequests.updateAndGet { requests ->
            if (key !in requests.keys) {
                requests + (key to getPaletteAsync(coilRequest))
            } else {
                requests
            }
        }.getValue(key).await()
    }

    private fun getPaletteAsync(coilRequest: Any?): Deferred<Palette?> {
        return coroutineScope.async(
            context = Dispatchers.Default,
            start = CoroutineStart.LAZY
        ) {
            computePalette(coilRequest)
        }
    }

    private suspend fun computePalette(coilRequest: Any?): Palette? {
        val bitmap = imageLoader
            .execute(
                ImageRequest.Builder(context)
                    .data(coilRequest)
                    .build()
            )
            .drawable
            ?.toBitmap()

        return if (bitmap != null) {
            Palette.from(bitmap).generate()
        } else {
            null
        }
    }

    private sealed class ColorKey {
        data class Album(val albumId: String) : ColorKey()
    }
}

@Composable
fun ColorRepository.rememberPaletteAsStateOf(album: Album): State<Palette?> {
    val state = remember { mutableStateOf<Palette?>(null) }
    LaunchedEffect(album) {
        state.value = paletteOf(album)
    }
    return state
}
