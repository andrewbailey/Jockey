package dev.andrewbailey.ipc.util

private const val CHUNK_OVERHEAD_BYTES = 512
internal const val BINDER_TRANSACTION_LIMIT_BYTES = 1_000_000 // 1 MB
internal const val MAX_CHUNK_SIZE_BYTES = BINDER_TRANSACTION_LIMIT_BYTES - CHUNK_OVERHEAD_BYTES
