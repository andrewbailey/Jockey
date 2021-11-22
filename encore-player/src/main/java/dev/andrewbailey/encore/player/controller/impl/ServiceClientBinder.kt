package dev.andrewbailey.encore.player.controller.impl

import android.content.ComponentName
import android.content.Context
import android.content.Context.BIND_AUTO_CREATE
import android.content.Context.BIND_WAIVE_PRIORITY
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import dev.andrewbailey.encore.model.MediaObject
import dev.andrewbailey.encore.player.MediaPlayerService
import dev.andrewbailey.encore.player.binder.ServiceBidirectionalMessenger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

internal class ServiceClientBinder<M : MediaObject>(
    private val context: Context,
    private val serviceClass: Class<out MediaPlayerService<M>>
) {

    private val serviceConnection = Connection()

    private val _serviceBinder = MutableStateFlow<ServiceBidirectionalMessenger<M>?>(null)
    val serviceBinder: StateFlow<ServiceBidirectionalMessenger<M>?>
        get() = _serviceBinder

    fun bind() {
        val intent = Intent(context, serviceClass)
        context.bindService(intent, serviceConnection, BIND_AUTO_CREATE or BIND_WAIVE_PRIORITY)
        context.startService(intent)
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

            _serviceBinder.value = ServiceBidirectionalMessenger(service)
        }
    }

}
