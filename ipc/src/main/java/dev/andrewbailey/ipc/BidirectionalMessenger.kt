package dev.andrewbailey.ipc

import android.os.IBinder
import android.os.Messenger
import android.os.Parcelable
import dev.andrewbailey.ipc.impl.IpcReceiver
import dev.andrewbailey.ipc.impl.IpcSender
import dev.andrewbailey.ipc.impl.MessageMetadata

typealias OnReceiveMessage<T, R> = BidirectionalMessenger<T, R>.(
    data: T,
    replyTo: BidirectionalMessenger<R, T>
) -> Unit

fun <T : Parcelable, R : Parcelable> bidirectionalMessenger(
    onReceiveMessage: OnReceiveMessage<T, R>
): BidirectionalMessenger<T, R> = BidirectionalMessengerHost(onReceiveMessage)

fun <T : Parcelable, R : Parcelable> bidirectionalMessenger(
    binder: IBinder
): BidirectionalMessenger<T, R> = BidirectionalMessengerClient(Messenger(binder))

sealed class BidirectionalMessenger<in T : Parcelable, out R : Parcelable> {

    private val sender by lazy { IpcSender<T>(messenger) }
    internal abstract val messenger: Messenger

    val binder: IBinder
        get() = messenger.binder

    val isAlive: Boolean
        get() = binder.isBinderAlive

    fun send(message: T, respondTo: BidirectionalMessenger<R, T>) {
        sender.sendMessage(message, respondTo.messenger)
    }

}

internal class BidirectionalMessengerHost<in T : Parcelable, out R : Parcelable>(
    private val onReceiveMessage: OnReceiveMessage<T, R>
) : BidirectionalMessenger<T, R>() {

    private val receiver = IpcReceiver(this::onReceiveMessage)

    override val messenger: Messenger
        get() = receiver.messenger

    private fun onReceiveMessage(message: MessageMetadata, data: T) {
        val replyTo = BidirectionalMessengerClient<R, T>(
            messenger = message.replyTo
        )

        onReceiveMessage(data, replyTo)
    }

}

internal class BidirectionalMessengerClient<in T : Parcelable, out R : Parcelable>(
    override val messenger: Messenger
) : BidirectionalMessenger<T, R>()
