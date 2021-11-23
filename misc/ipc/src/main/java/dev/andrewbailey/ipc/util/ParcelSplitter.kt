package dev.andrewbailey.ipc.util

import android.os.Parcel
import android.os.Parcelable

internal object ParcelSplitter {

    fun split(
        parcelable: Parcelable,
        chunkSizeInBytes: Int
    ): List<ByteArray> {
        val bytes = Parcel.obtain().use { parcel ->
            parcel.writeParcelable(parcelable, 0)
            parcel.marshall()
        }

        return bytes
            .toList()
            .chunked(chunkSizeInBytes) { it.toByteArray() }
    }

    fun <T : Parcelable> merge(chunks: List<ByteArray>, classLoader: ClassLoader): T? {
        val mergedBytes = ByteArray(size = chunks.sumOf { it.size })

        var index = 0
        chunks.forEach { chunk ->
            System.arraycopy(chunk, 0, mergedBytes, index, chunk.size)
            index += chunk.size
        }

        return Parcel.obtain().use {
            it.unmarshall(mergedBytes, 0, mergedBytes.size)
            it.setDataPosition(0)
            it.readParcelable(classLoader)
        }
    }

}
