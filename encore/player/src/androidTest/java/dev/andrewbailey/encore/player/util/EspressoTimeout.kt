package dev.andrewbailey.encore.player.util

import androidx.test.espresso.IdlingPolicies
import java.util.concurrent.TimeUnit
import org.junit.rules.Timeout
import org.junit.runner.Description
import org.junit.runners.model.Statement

class EspressoTimeout(
    private val timeout: Long,
    private val timeUnit: TimeUnit
) : Timeout(timeout, timeUnit) {

    override fun apply(base: Statement?, description: Description?): Statement {
        val baseStatement = super.apply(base, description)
        return object : Statement() {
            override fun evaluate() {
                val initialTimeout = IdlingPolicies.getMasterIdlingPolicy().idleTimeout
                val initialTimeUnit = IdlingPolicies.getMasterIdlingPolicy().idleTimeoutUnit
                try {
                    IdlingPolicies.setMasterPolicyTimeout(timeout / 2, timeUnit)
                    baseStatement.evaluate()
                } finally {
                    IdlingPolicies.setIdlingResourceTimeout(initialTimeout, initialTimeUnit)
                }
            }
        }
    }

}
