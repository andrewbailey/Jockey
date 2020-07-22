package dev.andrewbailey.ipc

import android.os.Handler
import android.os.HandlerThread
import android.os.Parcelable
import java.util.concurrent.Semaphore
import kotlin.test.assertEquals
import kotlin.test.assertNotSame
import kotlinx.android.parcel.Parcelize
import org.junit.After
import org.junit.Before
import org.junit.Test

private const val TIMEOUT_MS = 5_000L

class BidirectionalMessengerTest {

    private lateinit var clientThread: HandlerThread
    private lateinit var hostThread: HandlerThread

    private lateinit var client: BidirectionalMessenger<Response, Request>
    private lateinit var host: BidirectionalMessenger<Request, Response>

    private val hostResponses = mutableMapOf<String, Response>()
    private val clientResponses = mutableMapOf<String, Request>()

    private val hostReceivedMessages = mutableListOf<Request>()
    private val clientReceivedMessages = mutableListOf<Response>()

    @Before
    fun setUp() {
        clientThread = HandlerThread("BidirectionalMessengerTest-clientThread")
        hostThread = HandlerThread("BidirectionalMessengerTest-hostThread")

        clientThread.start()
        hostThread.start()

        host = bidirectionalMessenger(Handler(hostThread.looper)) { data, replyTo ->
            synchronized(hostReceivedMessages) {
                hostReceivedMessages += data
            }
            hostResponses[data.key]?.let { replyTo.send(it, this) }
        }

        client = bidirectionalMessenger(Handler(clientThread.looper)) { data, replyTo ->
            synchronized(clientReceivedMessages) {
                clientReceivedMessages += data
            }
            clientResponses[data.key]?.let { replyTo.send(it, this) }
        }
    }

    @After
    fun tearDown() {
        clientThread.quit()
        hostThread.quit()
    }

    @Test(timeout = TIMEOUT_MS)
    fun sendMessageToHostSendsDataToMessenger() {
        val message = Request(
            key = "small-message",
            blob = "Hello World!".toByteArray()
        )

        host.send(message, client)
        hostThread.waitForIdleSync()
        clientThread.waitForIdleSync()

        assertEquals(
            message = "hostReceivedMessages did not contain the expected value",
            expected = listOf(message),
            actual = hostReceivedMessages
        )

        assertNotSame(
            message = "The received message should be a copy of the original",
            illegal = message,
            actual = hostReceivedMessages.first()
        )

        assertEquals(
            message = "clientReceivedMessages should be empty",
            expected = emptyList<Response>(),
            actual = clientReceivedMessages
        )
    }

    @Test(timeout = TIMEOUT_MS)
    fun sendLargeMessageToHostSendsChunkedDataToMessenger() {
        val message = Request(
            key = "large-message",
            blob = ByteArray(1_500_000) { it.toByte() }
        )

        host.send(message, client)
        hostThread.waitForIdleSync()
        clientThread.waitForIdleSync()

        assertEquals(
            message = "hostReceivedMessages did not contain the expected value",
            expected = listOf(message),
            actual = hostReceivedMessages
        )

        assertNotSame(
            message = "The received message should be a copy of the original",
            illegal = message,
            actual = hostReceivedMessages.first()
        )

        assertEquals(
            message = "clientReceivedMessages should be empty",
            expected = emptyList<Response>(),
            actual = clientReceivedMessages
        )
    }

    @Test(timeout = TIMEOUT_MS)
    fun sendMultipleLargeMessagesInParallelCombinesCorrectChunks() {
        val message1 = Request(
            key = "thread1",
            blob = ByteArray(5_000_000) { 1 }
        )

        val message2 = Request(
            key = "thread2",
            blob = ByteArray(5_000_000) { 2 }
        )

        val threadInitGate = Semaphore(0)
        val messagingGate = Semaphore(0)
        val threadCompletionGate = Semaphore(0)

        var thread1Exception: Exception? = null
        var thread2Exception: Exception? = null

        Thread {
            // Wait for both threads to start before doing any work
            threadInitGate.release()
            messagingGate.acquire()

            try {
                host.send(message1, client)
            } catch (e: Exception) {
                thread1Exception = e
            } finally {
                threadCompletionGate.release()
            }
        }.start()

        Thread {
            // Wait for both threads to start before doing any work
            threadInitGate.release()
            messagingGate.acquire()

            try {
                host.send(message2, client)
            } catch (e: Exception) {
                thread2Exception = e
            } finally {
                threadCompletionGate.release()
            }
        }.start()

        // Wait for both threads to start
        threadInitGate.acquire(2)

        // Allow threads to begin sending messages
        messagingGate.release(2)

        // Wait for both threads to complete before making assertions about the results
        threadCompletionGate.acquire(2)

        thread1Exception?.let { throw it }
        thread2Exception?.let { throw it }

        assertEquals(
            message = "The host did not receive the correct messages (either order is valid)",
            expected = setOf(message1, message2),
            actual = hostReceivedMessages.toSet()
        )

        assertEquals(
            message = "The host did not receive the expected number of messages",
            expected = 2,
            actual = hostReceivedMessages.size
        )
    }

    @Test(timeout = TIMEOUT_MS)
    fun hostAndClientCanPerformConversation() {
        val message0 = Request(
            key = "message0",
            blob = ByteArray(0)
        )

        val message1 = Response(
            key = "message1",
            blob = "We're no strangers to love",
            code = 0x750c38c3
        )
        val message2 = Request(
            key = "message2",
            blob = "You know the rules and so do I".toByteArray()
        )

        val message3 = Response(
            key = "message3",
            blob = "A full commitment's what I'm thinking of",
            code = 0xd5a05dc4
        )
        val message4 = Request(
            key = "message4",
            blob = "You wouldn't get this from any other guy".toByteArray()
        )

        hostResponses += "message0" to message1
        clientResponses += "message1" to message2
        hostResponses += "message2" to message3
        clientResponses += "message3" to message4

        host.send(message0, client)

        // Wait for messages 0 through 4 to be received, with a timeout of a couple seconds to
        // make sure the conversation happens in a timely fashion.
        val t0 = System.currentTimeMillis()
        val conversationTimeoutMs = 2_000
        fun dT() = System.currentTimeMillis() - t0
        while (clientReceivedMessages.size + hostReceivedMessages.size < 5 &&
            dT() < conversationTimeoutMs) {
            clientThread.waitForIdleSync()
            hostThread.waitForIdleSync()
        }

        // Idle once more to ensure that any extra messages that have been enqueued are sent.
        clientThread.waitForIdleSync()
        hostThread.waitForIdleSync()

        assertEquals(
            message = "The host did not receive its messages correctly",
            expected = listOf(message0, message2, message4),
            actual = hostReceivedMessages
        )

        assertEquals(
            message = "The client did not receive its messages correctly",
            expected = listOf(message1, message3),
            actual = clientReceivedMessages
        )
    }

}

@Parcelize
data class Request(
    val key: String,
    val blob: ByteArray
) : Parcelable {
    override fun equals(other: Any?): Boolean {
        return other is Request &&
                other.key == this.key &&
                other.blob.contentEquals(this.blob)
    }

    override fun hashCode(): Int {
        return key.hashCode() + 31 * blob.contentHashCode()
    }

    override fun toString(): String {
        return "Request(key = $key, blob = [${blob.toTruncatedString()}])"
    }
}

@Parcelize
data class Response(
    val key: String,
    val blob: String,
    val code: Long
) : Parcelable

private fun ByteArray.toTruncatedString(maxItems: Int = 100): String {
    return if (size > maxItems) {
        "${take(maxItems).joinToString()}, ... (${size - maxItems} more items)"
    } else {
        joinToString()
    }
}
