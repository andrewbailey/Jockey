package dev.andrewbailey.ipc.impl

import android.os.*
import dev.andrewbailey.ipc.impl.Chunk.PartialChunk
import dev.andrewbailey.ipc.impl.Chunk.WholeChunk
import dev.andrewbailey.ipc.util.ParcelSplitter
import dev.andrewbailey.ipc.util.sizeInBytes

internal class IpcSender<in T : Parcelable>(
    private val messenger: Messenger
) {

    constructor(binder: IBinder) : this(Messenger(binder))

    fun sendMessage(message: T, respondTo: Messenger) {
        synchronized(messenger) {
            chunk(message).forEach { chunk ->
                messenger.send(Message.obtain().apply {
                    data = Bundle().apply {
                        putParcelable(KEY_PAYLOAD, chunk)
                    }
                    replyTo = respondTo
                })
            }
        }
    }

    private fun chunk(message: T): List<Chunk<T>> {
        return when {
            message.sizeInBytes() <= MAX_CHUNK_SiZE_BYTES -> {
                listOf(message.singleChunk())
            }
            else -> {
                message.multipleChunks(MAX_CHUNK_SiZE_BYTES)
            }
        }
    }

    private fun T.singleChunk(): WholeChunk<T> {
        return WholeChunk(contents = this)
    }

    private fun T.multipleChunks(chunkSizeInBytes: Int): List<PartialChunk<T>> {
        val chunks = ParcelSplitter.split(this, chunkSizeInBytes)

        return chunks.mapIndexed { index, chunk ->
                PartialChunk<T>(
                    chunkNumber = index,
                    numberOfChunks = chunks.size,
                    contents = chunk
                )
            }
    }

    companion object {
        internal const val KEY_PAYLOAD = "Chunk.PAYLOAD"

        private const val MAX_CHUNK_SiZE_KB = 950
        private const val MAX_CHUNK_SiZE_BYTES = MAX_CHUNK_SiZE_KB * 1000
    }
}
