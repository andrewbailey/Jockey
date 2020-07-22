package dev.andrewbailey.ipc.impl

import android.os.Parcel
import android.os.Parcelable
import dev.andrewbailey.ipc.util.MAX_CHUNK_SIZE_BYTES
import dev.andrewbailey.ipc.util.ParcelSplitter
import dev.andrewbailey.ipc.util.sizeInBytes
import java.util.UUID

internal sealed class Chunk<out T : Parcelable> : Parcelable {

    companion object {
        fun <T : Parcelable> createChunks(message: T): List<Chunk<T>> {
            return when {
                message.sizeInBytes() <= MAX_CHUNK_SIZE_BYTES -> {
                    listOf(message.singleChunk())
                }
                else -> {
                    message.multipleChunks(MAX_CHUNK_SIZE_BYTES)
                }
            }
        }

        private fun <T : Parcelable> T.singleChunk(): WholeChunk<T> {
            return WholeChunk(contents = this)
        }

        private fun <T : Parcelable> T.multipleChunks(
            chunkSizeInBytes: Int
        ): List<PartialChunk<T>> {
            val chunkId = UUID.randomUUID()
            val chunks = ParcelSplitter.split(this, chunkSizeInBytes)

            return chunks.mapIndexed { index, chunk ->
                PartialChunk<T>(
                    chunkId = chunkId,
                    chunkNumber = index,
                    numberOfChunks = chunks.size,
                    contents = chunk
                )
            }
        }
    }

    data class WholeChunk<out T : Parcelable>(
        val contents: T
    ) : Chunk<T>() {

        constructor(source: Parcel) : this(
            contents = source.readParcelable<T>(WholeChunk::class.java.classLoader)!!
        )

        override fun writeToParcel(dest: Parcel, flags: Int) {
            dest.writeParcelable(contents, flags)
        }

        override fun describeContents() = 0

        companion object CREATOR : Parcelable.Creator<WholeChunk<*>> {
            override fun createFromParcel(source: Parcel) = WholeChunk<Parcelable>(source)

            override fun newArray(size: Int): Array<WholeChunk<*>?> = arrayOfNulls(size)
        }
    }

    @Suppress("ArrayInDataClass")
    data class PartialChunk<out T : Parcelable>(
        val chunkId: UUID,
        val chunkNumber: Int,
        val numberOfChunks: Int,
        val contents: ByteArray
    ) : Chunk<T>() {

        constructor(source: Parcel) : this(
            chunkId = UUID.fromString(source.readString()),
            chunkNumber = source.readInt(),
            numberOfChunks = source.readInt(),
            contents = ByteArray(source.readInt()).also {
                source.readByteArray(it)
            }
        )

        override fun writeToParcel(dest: Parcel, flags: Int) {
            dest.apply {
                writeString(chunkId.toString())
                writeInt(chunkNumber)
                writeInt(numberOfChunks)
                writeInt(contents.size)
                writeByteArray(contents)
            }
        }

        override fun describeContents() = 0

        companion object CREATOR : Parcelable.Creator<PartialChunk<*>> {
            override fun createFromParcel(source: Parcel) = PartialChunk<Parcelable>(source)

            override fun newArray(size: Int): Array<PartialChunk<*>?> = arrayOfNulls(size)
        }
    }
}
