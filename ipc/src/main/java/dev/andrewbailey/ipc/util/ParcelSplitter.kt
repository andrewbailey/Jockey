package dev.andrewbailey.ipc.util

import android.os.Parcel
import android.os.Parcelable

internal object ParcelSplitter {

    fun split(
        parcelable: Parcelable,
        chunkSizeInBytes: Int
    ): List<ByteArray> {
        val bytes = Parcel.obtain().use { parcel ->
            parcelable.writeToParcel(parcel, 0)
            parcel.marshall()
        }

        return bytes
            .toList()
            .chunked(chunkSizeInBytes) { it.toByteArray() }
    }

    inline fun <reified T : Parcelable> merge(chunks: List<ByteArray>): T? {
        return merge(
            chunks,
            T::class.java.classLoader!!
        ) as T?
    }

    fun merge(chunks: List<ByteArray>, classLoader: ClassLoader): Parcelable? {
        val mergedBytes = ByteArray(size = chunks.sumBy { it.size })

        var index = 0
        chunks.forEach { chunk ->
            chunk.forEach { byte ->
                mergedBytes[index++] = byte
            }
        }

        return Parcel.obtain().use {
            it.unmarshall(mergedBytes, 0, mergedBytes.size)
            it.readParcelable(classLoader)
        }
    }

}
