package dev.andrewbailey

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.kotlin.dsl.withGroovyBuilder

private const val EXPLICIT_API = "-Xexplicit-api=strict"

/**
 * This plugin enables Kotlin's Explicit API mode for the project. This includes a workaround that
 * affects Android projects. When you enable Kotlin's explicit API mode on an Android project, both
 * the production sources _and_ the test sources are subject to the restriction. This isn't ideal
 * since tests aren't part of an API surface and don't benefit by having these checks. This plugin
 * addresses that by only providing the compiler flag for non-test Kotlin compile tasks.
 *
 * TODO: Delete this plugin when Android projects support explicit API mode.
 *   See [KT-37652](https://youtrack.jetbrains.com/issue/KT-37652).
 */
class ExplicitApiModePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.tasks
            .matching { it.isKotlinCompileTask }
            .configureEach {
                configureExplicitApiMode()
            }
    }
}

private fun Task.configureExplicitApiMode() {
    if (!project.isRunningInIde) {
        /*
         * This logic works for pure Gradle builds (tested with Kotlin plugin version 1.5.31).
         * Android Studio, however, will not respect that the feature is disabled for test builds
         * and will show false positives in the editor for test files.
         *
         * As a workaround, disable this check entirely in the editor. It'll still apply for
         * builds from the terminal, which at least lets this check happen in CI.
         */
        if (!isTestTask) {
            enableExplicitApiMode()
        } else {
            disableExplicitApiMode()
        }
    }
}

private val Project.isRunningInIde: Boolean
    get() = "android.injected.invoked.from.ide" in properties ||
            "intellij" in System.getenv("XPC_SERVICE_NAME").orEmpty() ||
            System.getenv("IDEA_INITIAL_DIRECTORY") != null

private val Task.isKotlinCompileTask: Boolean
    get() = "KotlinCompile" in javaClass.name

private val Task.isTestTask: Boolean
    get() = name.contains("test", ignoreCase = true)

private fun Task.enableExplicitApiMode() {
    if (EXPLICIT_API !in kotlinOptions) {
        kotlinOptions += EXPLICIT_API
    }
}

private fun Task.disableExplicitApiMode() {
    if (EXPLICIT_API in kotlinOptions) {
        kotlinOptions -= EXPLICIT_API
    }
}

@Suppress("UNCHECKED_CAST")
private var Task.kotlinOptions: Collection<String>
    get() = withGroovyBuilder {
        lateinit var result: Collection<String>
        "kotlinOptions" {
            result = getProperty("freeCompilerArgs") as Collection<String>
        }
        return result
    }
    set(value) {
        withGroovyBuilder {
            "kotlinOptions" {
                invokeMethod("setFreeCompilerArgs", value)
            }
        }
    }
