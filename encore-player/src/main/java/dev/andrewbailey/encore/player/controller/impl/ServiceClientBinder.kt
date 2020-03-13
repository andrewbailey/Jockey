package dev.andrewbailey.encore.player.controller.impl

import android.content.ComponentName
import android.content.Context
import android.content.Context.BIND_AUTO_CREATE
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import dev.andrewbailey.encore.player.MediaPlayerService
import dev.andrewbailey.encore.player.binder.ServiceBidirectionalMessenger
import dev.andrewbailey.encore.player.util.Resource
import dev.andrewbailey.ipc.bidirectionalMessenger

internal class ServiceClientBinder(
    private val context: Context,
    private val serviceClass: Class<out MediaPlayerService>
) {

    private val serviceConnection = Connection()

    val serviceBinder = Resource<ServiceBidirectionalMessenger>()

    fun bind() {
        context.bindService(Intent(context, serviceClass), serviceConnection, BIND_AUTO_CREATE)
    }

    fun unbind() {
        context.unbindService(serviceConnection)
    }

    inner class Connection : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName) {
            serviceBinder.clearResource()
        }

        override fun onServiceConnected(name: ComponentName, service: IBinder?) {
            requireNotNull(service) {
                "Cannot bind to service $name, because it does not support binding."
            }

            serviceBinder.setResource(bidirectionalMessenger(service))
        }
    }

}
