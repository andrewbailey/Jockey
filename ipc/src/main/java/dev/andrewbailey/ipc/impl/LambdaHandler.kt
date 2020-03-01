package dev.andrewbailey.ipc.impl

import android.os.Handler
import android.os.Message

internal class LambdaHandler(
    private val handleMessage: (Message) -> Unit
) : Handler() {
    override fun handleMessage(msg: Message) {
        handleMessage.invoke(msg)
    }
}
