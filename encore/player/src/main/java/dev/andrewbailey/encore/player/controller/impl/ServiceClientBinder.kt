package dev.andrewbailey.encore.player.controller.impl

import android.content.ComponentName
import android.content.Context
import android.content.Context.BIND_AUTO_CREATE
import android.content.Context.BIND_WAIVE_PRIORITY
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import dev.andrewbailey.encore.model.MediaObject
import dev.andrewbailey.encore.player.binder.ServiceBidirectionalMessenger
import dev.andrewbailey.encore.player.controller.impl.ServiceBindingResource.BindingState
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

internal class ServiceClientBinder<M : MediaObject>(
    private val context: Context,
    private val componentName: ComponentName
) {

    private val serviceConnection = Connection()
    private val _isBound = atomic(false)
    val isBound: Boolean get() = _isBound.value

    private val _serviceBinder = MutableStateFlow<ServiceBidirectionalMessenger<M>?>(null)
    val serviceBinder: StateFlow<ServiceBidirectionalMessenger<M>?>
        get() = _serviceBinder

    internal val idlingResource = ServiceBindingResource(
        currentState = BindingState.NotBound,
        desiredState = BindingState.NotBound
    )

    fun bind() {
        val intent = Intent().apply {
            component = componentName
        }

        val wasBound = _isBound.getAndSet(true)
        if (!wasBound) {
            idlingResource.desiredState = BindingState.Bound

            context.bindService(intent, serviceConnection, BIND_AUTO_CREATE or BIND_WAIVE_PRIORITY)
            context.startService(intent)
        } else {
            context.startService(intent)
        }
    }

    fun unbind() {
        val wasBound = _isBound.getAndSet(false)
        if (wasBound) {
            idlingResource.desiredState = BindingState.NotBound
            context.unbindService(serviceConnection)
            idlingResource.currentState = BindingState.NotBound
        }
    }

    inner class Connection : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName) {
            _serviceBinder.value = null
            idlingResource.currentState = BindingState.NotBound
        }

        override fun onServiceConnected(name: ComponentName, service: IBinder?) {
            requireNotNull(service) {
                "Cannot bind to service $name, because it does not support binding."
            }

            _serviceBinder.value = ServiceBidirectionalMessenger(service)
            idlingResource.currentState = BindingState.Bound
        }
    }

}
