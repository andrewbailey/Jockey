package dev.andrewbailey.ipc.impl

import android.os.IBinder
import android.os.Message
import android.os.Messenger
import android.os.Parcelable
import dev.andrewbailey.ipc.impl.Chunk.PartialChunk
import dev.andrewbailey.ipc.impl.Chunk.WholeChunk
import dev.andrewbailey.ipc.impl.IpcSender.Companion.KEY_PAYLOAD
import dev.andrewbailey.ipc.util.ParcelSplitter
import java.util.UUID

internal class IpcReceiver<out T : Parcelable>(
    private val onReceiveMessage: (MessageMetadata, T) -> Unit
) {

    private val handler = LambdaHandler(::handleMessage)
    private val chunks = mutableMapOf<UUID, MutableList<PartialChunk<T>>>()

    val messenger = Messenger(handler)

    val binder: IBinder
        get() = messenger.binder

    private fun handleMessage(message: Message) {
        message.data.classLoader = javaClass.classLoader
        val chunk = requireNotNull(message.data.getParcelable<Chunk<T>>(KEY_PAYLOAD)) {
            "Received a message that did not contain a chunk."
        }

        when (chunk) {
            is WholeChunk -> onReceiveMessage(MessageMetadata(message), chunk.contents)
            is PartialChunk -> onReceivePartialMessage(message, chunk)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun onReceivePartialMessage(message: Message, chunk: PartialChunk<T>) {
        val relatedChunks = chunks.getOrPut(chunk.chunkId) { mutableListOf() }
        relatedChunks += chunk

        if (relatedChunks.size == chunk.numberOfChunks) {
            chunks.remove(chunk.chunkId)
            val fullPayload = ParcelSplitter.merge(
                chunks = relatedChunks.sortedBy { it.chunkNumber }
                    .map { it.contents },
                classLoader = PartialChunk::class.java.classLoader!!
            ) ?: return

            onReceiveMessage(MessageMetadata(message), fullPayload as T)
        }
    }

}
