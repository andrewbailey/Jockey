package dev.andrewbailey.ipc

import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Parcel
import android.os.Parcelable
import dev.andrewbailey.ipc.impl.Chunk
import dev.andrewbailey.ipc.impl.Chunk.PartialChunk
import dev.andrewbailey.ipc.impl.Chunk.WholeChunk
import dev.andrewbailey.ipc.util.BINDER_TRANSACTION_LIMIT_BYTES
import dev.andrewbailey.ipc.util.ParcelSplitter
import java.util.UUID

public typealias OnReceiveMessage<T, R> = BidirectionalMessenger<T, R>.(
    data: T,
    replyTo: BidirectionalMessenger<R, T>
) -> Unit

public fun <T : Parcelable, R : Parcelable> bidirectionalMessenger(
    handler: Handler = Handler(Looper.getMainLooper()),
    onReceiveMessage: OnReceiveMessage<T, R>
): BidirectionalMessenger<T, R> = BidirectionalMessengerHost(handler, onReceiveMessage)

public fun <T : Parcelable, R : Parcelable> bidirectionalMessenger(
    binder: IBinder
): BidirectionalMessenger<T, R> = BidirectionalMessengerClient(binder)

public sealed class BidirectionalMessenger<in T : Parcelable, out R : Parcelable> {

    public abstract val binder: IBinder

    public val isAlive: Boolean
        get() = binder.isBinderAlive

    public fun send(message: T, respondTo: BidirectionalMessenger<R, T>) {
        Chunk.createChunks(message).forEach { chunk ->
            val data = Parcel.obtain()
            val reply = Parcel.obtain()
            try {
                data.setDataCapacity(BINDER_TRANSACTION_LIMIT_BYTES)
                data.writeParcelable(chunk, 0)
                data.writeStrongBinder(respondTo.binder)

                binder.transact(USER_REQUEST_TRANSACTION, data, reply, 0)

                reply.readException()
            } finally {
                data.recycle()
                reply.recycle()
            }
        }
    }

}

internal class BidirectionalMessengerHost<in T : Parcelable, out R : Parcelable>(
    private val handler: Handler,
    private val onReceiveMessage: OnReceiveMessage<T, R>
) : BidirectionalMessenger<T, R>() {

    private val chunks = mutableMapOf<UUID, MutableList<PartialChunk<T>>>()

    override val binder: IBinder = object : Binder() {
        override fun onTransact(code: Int, data: Parcel, reply: Parcel?, flags: Int): Boolean {
            return if (code == USER_REQUEST_TRANSACTION) {
                onReceiveChunk(
                    chunk = data.readParcelable(javaClass.classLoader)!!,
                    replyTo = data.readStrongBinder()
                )
                true
            } else {
                super.onTransact(code, data, reply, flags)
            }
        }
    }

    private fun onReceiveChunk(chunk: Chunk<T>, replyTo: IBinder) {
        when (chunk) {
            is WholeChunk -> onReceiveFullMessage(chunk.contents, replyTo)
            is PartialChunk -> onReceivePartialMessage(chunk, replyTo)
        }
    }

    private fun onReceivePartialMessage(chunk: PartialChunk<T>, replyTo: IBinder) {
        val relatedChunks = chunks.getOrPut(chunk.chunkId) { mutableListOf() }
        relatedChunks += chunk

        if (relatedChunks.size == chunk.numberOfChunks) {
            chunks.remove(chunk.chunkId)
            val fullPayload = ParcelSplitter.merge<T>(
                chunks = relatedChunks.sortedBy { it.chunkNumber }
                    .map { it.contents },
                classLoader = javaClass.classLoader!!
            )!!

            onReceiveFullMessage(fullPayload, replyTo)
        }
    }

    private fun onReceiveFullMessage(request: T, replyTo: IBinder) {
        handler.post {
            onReceiveMessage(request, BidirectionalMessengerClient(replyTo))
        }
    }

}

internal class BidirectionalMessengerClient<in T : Parcelable, out R : Parcelable>(
    override val binder: IBinder
) : BidirectionalMessenger<T, R>()

internal const val USER_REQUEST_TRANSACTION = IBinder.FIRST_CALL_TRANSACTION
