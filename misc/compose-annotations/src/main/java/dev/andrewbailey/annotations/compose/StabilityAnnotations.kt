package dev.andrewbailey.annotations.compose

import androidx.compose.runtime.StableMarker
import kotlin.annotation.AnnotationRetention.BINARY
import kotlin.annotation.AnnotationTarget.CLASS
import kotlin.annotation.AnnotationTarget.FUNCTION
import kotlin.annotation.AnnotationTarget.PROPERTY
import kotlin.annotation.AnnotationTarget.PROPERTY_GETTER

/**
 * Marks all of the functions and properties in the class as [ComposeStableFunction]. This also
 * implies that the equality of two objects never changes over the lifetime of either object.
 *
 * Enums, Strings, and primitives are implicitly considered to be stable.
 */
@Target(CLASS)
@Retention(BINARY)
@StableMarker
public annotation class ComposeStableClass

/**
 * Marks a function as [androidx.compose.runtime.Stable]. Stable functions return the same result
 * when called with the same inputs. The input types and return type of a stable function should
 * also be stable.
 */
@Target(FUNCTION)
@Retention(BINARY)
@StableMarker
public annotation class ComposeStableFunction

/**
 * Marks a property as [androidx.compose.runtime.Stable]. Stable properties are either immutable and
 * read-only, or will notify the composition when their value changes.
 */
@Target(PROPERTY_GETTER, PROPERTY)
@Retention(BINARY)
@StableMarker
public annotation class ComposeStableProperty
