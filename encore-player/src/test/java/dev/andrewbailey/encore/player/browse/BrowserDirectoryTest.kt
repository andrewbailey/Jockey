package dev.andrewbailey.encore.player.browse

import kotlin.test.Test
import kotlin.test.assertFailsWith

class BrowserDirectoryTest {

    @Test
    fun `staticPath throws with reserved characters in id`() {
        val illegalIds = listOf("foo/", "/foo", "f/oo", "f@oo", "f[oo", "foo]")

        illegalIds.forEach { testCase ->
            val dir = BrowserDirectory("")
            assertFailsWith<IllegalArgumentException>(
                message = "Using id '$testCase' should throw an exception"
            ) {
                dir.staticPath(
                    path = BrowserDirectory.BrowserPath(
                        id = testCase,
                        name = "Foo"
                    ),
                    pathContents = {}
                )
            }
        }
    }

    @Test
    fun `dynamicPath throws with reserved characters in id`() {
        val illegalIds = listOf("foo/", "/foo", "f/oo", "f@oo", "f[oo", "foo]")

        illegalIds.forEach { testCase ->
            val dir = BrowserDirectory("")
            assertFailsWith<IllegalArgumentException>(
                message = "Using id '$testCase' should throw an exception"
            ) {
                dir.dynamicPaths(
                    identifier = testCase,
                    paths = { emptyList() },
                    pathContents = {}
                )
            }
        }
    }

    @Test
    fun `mediaItems throws with reserved characters in id`() {
        val illegalIds = listOf("foo/", "/foo", "f/oo", "f@oo", "f[oo", "foo]")

        illegalIds.forEach { testCase ->
            val dir = BrowserDirectory("")
            assertFailsWith<IllegalArgumentException>(
                message = "Using id '$testCase' should throw an exception"
            ) {
                dir.mediaItems(
                    identifier = testCase,
                    loadItems = { emptyList() }
                )
            }
        }
    }

}
