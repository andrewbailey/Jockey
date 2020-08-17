package dev.andrewbailey.ipc.util

import android.os.Parcel
import android.os.Parcelable
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind.EXACTLY_ONCE
import kotlin.contracts.contract

@OptIn(ExperimentalContracts::class)
internal inline fun <T> Parcel.use(action: (Parcel) -> T): T {
    contract { callsInPlace(action, EXACTLY_ONCE) }
    try {
        return action(this)
    } finally {
        recycle()
    }
}

internal fun Parcelable.sizeInBytes(): Int {
    return Parcel.obtain().use { parcel ->
        writeToParcel(parcel, 0)
        parcel.dataSize()
    }
}
