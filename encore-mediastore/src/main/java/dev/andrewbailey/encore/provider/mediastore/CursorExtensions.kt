package dev.andrewbailey.encore.provider.mediastore

import android.content.Context
import android.database.Cursor
import android.net.Uri

internal fun Context.query(
    uri: Uri,
    projection: List<String>? = null,
    sortOrder: String? = null
) = contentResolver.query(uri, projection?.toTypedArray(), null, null, sortOrder)

internal fun Context.query(
    uri: Uri,
    projection: List<String>? = null,
    selection: String,
    selectionArgs: Array<String>? = null,
    sortOrder: String? = null
) = contentResolver.query(uri, projection?.toTypedArray(), selection, selectionArgs, sortOrder)

internal inline fun <T> Cursor.toList(transform: (Cursor) -> T): List<T> {
    moveToPosition(-1)
    return List(count) {
        moveToNext()
        transform(this)
    }
}

internal fun Cursor.getShort(columnName: String): Short =
    getShort(getColumnIndexOrThrow(columnName))
internal fun Cursor.getInt(columnName: String): Int =
    getInt(getColumnIndexOrThrow(columnName))
internal fun Cursor.getLong(columnName: String): Long =
    getLong(getColumnIndexOrThrow(columnName))
internal fun Cursor.getFloat(columnName: String): Float =
    getFloat(getColumnIndexOrThrow(columnName))
internal fun Cursor.getDouble(columnName: String): Double =
    getDouble(getColumnIndexOrThrow(columnName))
internal fun Cursor.getString(columnName: String): String? =
    getString(getColumnIndexOrThrow(columnName))
