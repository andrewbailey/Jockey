package dev.andrewbailey.ipc.impl

import android.os.Parcelable
import java.util.UUID
import kotlinx.android.parcel.Parcelize

internal sealed class Chunk<out T> : Parcelable {

    @Parcelize
    data class WholeChunk<out T : Parcelable>(
        val contents: T
    ) : Chunk<T>()

    @Parcelize
    @Suppress("ArrayInDataClass")
    data class PartialChunk<out T : Parcelable>(
        val chunkId: UUID = UUID.randomUUID(),
        val chunkNumber: Int,
        val numberOfChunks: Int,
        val contents: ByteArray
    ) : Chunk<T>()

}
