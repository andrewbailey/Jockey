package dev.andrewbailey.encore.provider.mediastore.artwork

import android.graphics.BitmapFactory

internal fun determineScaleFactor(
    path: String,
    widthPx: Int?,
    heightPx: Int?
): Int = determineScaleFactor(widthPx, heightPx) {
    BitmapFactory.decodeFile(path, it)
}

internal fun determineScaleFactor(
    bytes: ByteArray,
    widthPx: Int?,
    heightPx: Int?
): Int = determineScaleFactor(widthPx, heightPx) {
    BitmapFactory.decodeByteArray(bytes, 0, bytes.size, it)
}

internal inline fun determineScaleFactor(
    widthPx: Int?,
    heightPx: Int?,
    sizeGenerator: (BitmapFactory.Options) -> Unit
): Int {
    return if (widthPx == null && heightPx == null) {
        1
    } else {
        val bitmapSize = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
            sizeGenerator(this)
        }

        minOf(
            widthPx?.div(bitmapSize.outWidth) ?: Int.MAX_VALUE,
            widthPx?.div(bitmapSize.outHeight) ?: Int.MAX_VALUE
        )
    }
}
