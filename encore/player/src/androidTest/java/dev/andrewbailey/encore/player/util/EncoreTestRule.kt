package dev.andrewbailey.encore.player.util

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.test.espresso.Espresso
import androidx.test.espresso.IdlingRegistry
import androidx.test.platform.app.InstrumentationRegistry
import dev.andrewbailey.encore.model.MediaObject
import dev.andrewbailey.encore.player.EncoreTestService
import dev.andrewbailey.encore.player.MediaPlayerService
import dev.andrewbailey.encore.player.controller.EncoreController
import dev.andrewbailey.encore.player.controller.impl.EncoreControllerImpl
import dev.andrewbailey.encore.player.controller.impl.ServiceBindingResource.BindingState.Bound
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

class EncoreTestRule<M : MediaObject>(
    private val serviceClass: Class<out MediaPlayerService<M>>
) : TestRule {

    private val testContext: Context
        get() = InstrumentationRegistry.getInstrumentation().context

    private val serviceContext: Context
        get() = InstrumentationRegistry.getInstrumentation().context

    private var encoreController: EncoreControllerImpl<M>? = null

    override fun apply(base: Statement, description: Description): Statement {
        return object : Statement() {
            override fun evaluate() {
                try {
                    setUpController()
                    base.evaluate()
                } finally {
                    tearDownController()
                }
            }
        }
    }

    private fun setUpController() {
        synchronized(this) {
            check(encoreController == null) {
                "An EncoreController was already set up and cannot be reused."
            }

            encoreController = EncoreControllerImpl<M>(
                context = testContext,
                componentName = ComponentName(serviceContext, serviceClass)
            ).apply {
                IdlingRegistry.getInstance().register(clientBinder.idlingResource)
            }
        }
    }

    private fun tearDownController() {
        synchronized(this) {
            val controller = checkNotNull(encoreController) {
                "The EncoreController has already been destroyed."
            }

            waitForServiceBindingToSettle()

            val wasServiceBound = controller.clientBinder.idlingResource.desiredState == Bound
            if (wasServiceBound) {
                controller.clientBinder.unbind()
                waitForServiceBindingToSettle()
            }

            IdlingRegistry.getInstance().unregister(controller.clientBinder.idlingResource)
            encoreController = null

            testContext.stopService(
                Intent().apply {
                    component = ComponentName(serviceContext, serviceClass)
                }
            )
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()

            if (wasServiceBound) {
                throw AssertionError("The EncoreController did not release itself")
            }
        }
    }

    fun withEncore(
        bind: Boolean = true,
        action: (encoreController: EncoreController<M>) -> Unit
    ) {
        val controller = requireNotNull(encoreController) {
            "The EncoreController has not been created. Did you correctly set up the test rule?"
        }

        if (bind) {
            val token = controller.acquireToken()
            waitForServiceBindingToSettle()

            try {
                action(controller)
            } finally {
                controller.releaseToken(token)
                waitForServiceBindingToSettle()
            }
        } else {
            action(controller)
        }
    }

    private fun waitForServiceBindingToSettle() {
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        Espresso.onIdle()
    }

    companion object {
        operator fun invoke() = EncoreTestRule(EncoreTestService::class.java)
    }

}
