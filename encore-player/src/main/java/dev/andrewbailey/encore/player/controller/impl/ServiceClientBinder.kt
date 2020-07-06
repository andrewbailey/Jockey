package dev.andrewbailey.encore.player.controller.impl

import android.content.ComponentName
import android.content.Context
import android.content.Context.BIND_AUTO_CREATE
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import dev.andrewbailey.encore.player.MediaPlayerService
import dev.andrewbailey.encore.player.binder.ServiceBidirectionalMessenger
import dev.andrewbailey.ipc.bidirectionalMessenger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

internal class ServiceClientBinder(
    private val context: Context,
    private val serviceClass: Class<out MediaPlayerService>
) {

    private val serviceConnection = Connection()

    private val _serviceBinder = MutableStateFlow<ServiceBidirectionalMessenger?>(null)
    val serviceBinder: StateFlow<ServiceBidirectionalMessenger?>
        get() = _serviceBinder

    fun bind() {
        context.bindService(Intent(context, serviceClass), serviceConnection, BIND_AUTO_CREATE)
    }

    fun unbind() {
        context.unbindService(serviceConnection)
    }

    inner class Connection : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName) {
            _serviceBinder.value = null
        }

        override fun onServiceConnected(name: ComponentName, service: IBinder?) {
            requireNotNull(service) {
                "Cannot bind to service $name, because it does not support binding."
            }

            _serviceBinder.value = bidirectionalMessenger(service)
        }
    }

}
