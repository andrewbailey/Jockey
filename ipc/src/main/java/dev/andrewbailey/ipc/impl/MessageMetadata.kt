package dev.andrewbailey.ipc.impl

import android.os.Message
import android.os.Messenger

internal data class MessageMetadata(
    val replyTo: Messenger
) {

    constructor(message: Message) : this(
        replyTo = message.replyTo
    )

}
