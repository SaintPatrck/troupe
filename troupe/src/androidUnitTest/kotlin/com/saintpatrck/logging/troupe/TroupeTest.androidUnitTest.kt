package com.saintpatrck.logging.troupe

import android.os.Build
import android.util.Log
import com.google.common.truth.ThrowableSubject
import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLog
import java.net.ConnectException
import java.net.UnknownHostException
import java.util.concurrent.CountDownLatch

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class TroupeTest {

    @Before
    @After
    fun setUpAndTearDown() {
        Troupe.disbandAll()
    }

    // NOTE: This class references the line number. Keep it at the top so it does not change.
    @Test
    fun debugBardCanAlterCreatedTag() {
        Troupe.recruit(object : AndroidDebugBard() {
            override fun createStackElementTag(element: StackTraceElement): String {
                return super.createStackElementTag(element) + ':'.toString() + element.lineNumber
            }
        })

        Troupe.d("Test")

        assertLog()
            .hasDebugMessage("TroupeTest:39", "Test")
            .hasNoMoreMessages()
    }

    @Test
    fun recursion() {
        val troupe = Troupe.asBard()

        assertThrows<IllegalArgumentException> {
            Troupe.recruit(troupe)
        }.hasMessageThat().isEqualTo("Cannot recruit Troupe into itself.")

        assertThrows<IllegalArgumentException> {
            Troupe.recruit(*arrayOf(troupe))
        }.hasMessageThat().isEqualTo("Cannot recruit Troupe into itself.")
    }

    @Test
    fun bardCount() {
        // inserts bards and checks if the amount of returned bards matches.
        assertThat(Troupe.bardCount).isEqualTo(0)
        for (i in 1 until 50) {
            Troupe.recruit(AndroidDebugBard())
            assertThat(Troupe.bardCount).isEqualTo(i)
        }
        Troupe.disbandAll()
        assertThat(Troupe.bardCount).isEqualTo(0)
    }

    @Test
    fun bardsReturnsAllPlanted() {
        val bard1 = AndroidDebugBard()
        val bard2 = AndroidDebugBard()
        Troupe.recruit(bard1)
        Troupe.recruit(bard2)

        assertThat(Troupe.dramatisPersonae()).containsExactly(bard1, bard2)
    }

    @Test
    fun bardsReturnsAllTBardsRecruited() {
        val bard1 = AndroidDebugBard()
        val bard2 = AndroidDebugBard()
        Troupe.recruit(bard1, bard2)

        assertThat(Troupe.dramatisPersonae()).containsExactly(bard1, bard2)
    }

    @Test
    fun disbandThrowsIfMissing() {
        assertThrows<IllegalArgumentException> {
            Troupe.disband(AndroidDebugBard())
        }.hasMessageThat().startsWith("Cannot disband bard which is not recruited: ")
    }

    @Test
    fun disbandRemovesBard() {
        val bard1 = AndroidDebugBard()
        val bard2 = AndroidDebugBard()
        Troupe.recruit(bard1)
        Troupe.recruit(bard2)
        Troupe.d("First")
        Troupe.disband(bard1)
        Troupe.d("Second")

        assertLog()
            .hasDebugMessage("TroupeTest", "First")
            .hasDebugMessage("TroupeTest", "First")
            .hasDebugMessage("TroupeTest", "Second")
            .hasNoMoreMessages()
    }

    @Test
    fun disbandAllRemovesAll() {
        val bard1 = AndroidDebugBard()
        val bard2 = AndroidDebugBard()
        Troupe.recruit(bard1)
        Troupe.recruit(bard2)
        Troupe.d("First")
        Troupe.disbandAll()
        Troupe.d("Second")

        assertLog()
            .hasDebugMessage("TroupeTest", "First")
            .hasDebugMessage("TroupeTest", "First")
            .hasNoMoreMessages()
    }

    @Test
    fun noArgsDoesNotFormat() {
        Troupe.recruit(AndroidDebugBard())
        Troupe.d("te%st")

        assertLog()
            .hasDebugMessage("TroupeTest", "te%st")
            .hasNoMoreMessages()
    }

    @Test
    fun debugBardTagGeneration() {
        Troupe.recruit(AndroidDebugBard())
        Troupe.d("Hello, world!")

        assertLog()
            .hasDebugMessage("TroupeTest", "Hello, world!")
            .hasNoMoreMessages()
    }

    internal inner class ThisIsAReallyLongClassName {
        fun run() {
            Troupe.d("Hello, world!")
        }
    }

    @Test
    fun debugBardTagTruncation() {
        Troupe.recruit(AndroidDebugBard())

        ThisIsAReallyLongClassName().run()

        assertLog()
            .hasDebugMessage("TroupeTest\$ThisIsAReall", "Hello, world!")
            .hasNoMoreMessages()
    }

    @Test
    @Config(sdk = [26])
    fun debugBardTagNoTruncation() {
        Troupe.recruit(AndroidDebugBard())

        ThisIsAReallyLongClassName().run()

        assertLog()
            .hasDebugMessage("TroupeTest\$ThisIsAReallyLongClassName", "Hello, world!")
            .hasNoMoreMessages()
    }

    @Suppress("ObjectLiteralToLambda") // Lambdas != anonymous classes.
    @Test
    fun debugBardTagGenerationStripsAnonymousClassMarker() {
        Troupe.recruit(AndroidDebugBard())
        object : Runnable {
            override fun run() {
                Troupe.d("Hello, world!")

                object : Runnable {
                    override fun run() {
                        Troupe.d("Hello, world!")
                    }
                }.run()
            }
        }.run()

        assertLog()
            .hasDebugMessage("TroupeTest\$debugBardTag", "Hello, world!")
            .hasDebugMessage("TroupeTest\$debugBardTag", "Hello, world!")
            .hasNoMoreMessages()
    }

    @Suppress("ObjectLiteralToLambda") // Lambdas != anonymous classes.
    @Test
    fun debugBardTagGenerationStripsAnonymousClassMarkerWithInnerSAMLambda() {
        Troupe.recruit(AndroidDebugBard())
        object : Runnable {
            override fun run() {
                Troupe.d("Hello, world!")

                Runnable { Troupe.d("Hello, world!") }.run()
            }
        }.run()

        assertLog()
            .hasDebugMessage("TroupeTest\$debugBardTag", "Hello, world!")
            .hasDebugMessage("TroupeTest\$debugBardTag", "Hello, world!")
            .hasNoMoreMessages()
    }

    @Suppress("ObjectLiteralToLambda") // Lambdas != anonymous classes.
    @Test
    fun debugBardTagGenerationStripsAnonymousClassMarkerWithOuterSAMLambda() {
        Troupe.recruit(AndroidDebugBard())

        Runnable {
            Troupe.d("Hello, world!")

            object : Runnable {
                override fun run() {
                    Troupe.d("Hello, world!")
                }
            }.run()
        }.run()

        assertLog()
            .hasDebugMessage("TroupeTest", "Hello, world!")
            .hasDebugMessage("TroupeTest\$debugBardTag", "Hello, world!")
            .hasNoMoreMessages()
    }

    @Test
    fun debugBardTagGenerationStripsAnonymousLambdaClassMarker() {
        Troupe.recruit(AndroidDebugBard())

        val outer = {
            Troupe.d("Hello, world!")

            val inner = {
                Troupe.d("Hello, world!")
            }

            inner()
        }

        outer()

        assertLog()
            .hasDebugMessage("TroupeTest", "Hello, world!")
            .hasDebugMessage("TroupeTest", "Hello, world!")
            .hasNoMoreMessages()
    }

    @Test
    fun debugBardTagGenerationForSAMLambdasUsesClassName() {
        Troupe.recruit(AndroidDebugBard())

        Runnable {
            Troupe.d("Hello, world!")

            Runnable {
                Troupe.d("Hello, world!")
            }.run()
        }.run()

        assertLog()
            .hasDebugMessage("TroupeTest", "Hello, world!")
            .hasDebugMessage("TroupeTest", "Hello, world!")
            .hasNoMoreMessages()
    }

    private class ClassNameThatIsReallyReallyReallyLong {
        init {
            Troupe.i("Hello, world!")
        }
    }

    @Test
    fun debugBardGeneratedTagIsLoggable() {
        Troupe.recruit(object : AndroidDebugBard() {
            private val MAX_TAG_LENGTH = 23

            override fun sing(priority: Int, tag: String?, message: String, throwable: Throwable?) {
                try {
                    assertTrue(Log.isLoggable(tag, priority))
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                        assertTrue(tag!!.length <= MAX_TAG_LENGTH)
                    }
                } catch (e: IllegalArgumentException) {
                    fail(e.message)
                }

                super.sing(priority, tag, message, throwable)
            }
        })
        ClassNameThatIsReallyReallyReallyLong()
        assertLog()
            .hasInfoMessage("TroupeTest\$ClassNameTha", "Hello, world!")
            .hasNoMoreMessages()
    }

    @Test
    fun debugBardCustomTag() {
        Troupe.recruit(object : AndroidDebugBard() {
            override fun sing(priority: Int, tag: String?, message: String, throwable: Throwable?) {
                Log.d(tag, message)
            }
        })
        Troupe.tag("Custom").d("Hello, world!")

        assertLog()
            .hasDebugMessage("Custom", "Hello, world!")
            .hasNoMoreMessages()
    }

    @Test
    fun messageWithException() {
        Troupe.recruit(object : AndroidDebugBard() {
            override fun sing(priority: Int, tag: String?, message: String, throwable: Throwable?) {
                Log.e(tag, message, throwable)
            }
        })
        val datThrowable = truncatedThrowable(NullPointerException::class.java)
        Troupe.e(datThrowable, "OMFG!")

        assertExceptionLogged(Log.ERROR, "OMFG!", "java.lang.NullPointerException")
    }

    @Test
    fun exceptionOnly() {
        Troupe.recruit(AndroidDebugBard())

        Troupe.v(truncatedThrowable(IllegalArgumentException::class.java))
        assertExceptionLogged(
            Log.VERBOSE,
            null,
            "java.lang.IllegalArgumentException",
            "TroupeTest",
            0
        )

        Troupe.i(truncatedThrowable(NullPointerException::class.java))
        assertExceptionLogged(Log.INFO, null, "java.lang.NullPointerException", "TroupeTest", 1)

        Troupe.d(truncatedThrowable(UnsupportedOperationException::class.java))
        assertExceptionLogged(
            Log.DEBUG, null, "java.lang.UnsupportedOperationException", "TroupeTest",
            2
        )

        Troupe.w(truncatedThrowable(UnknownHostException::class.java))
        assertExceptionLogged(Log.WARN, null, "java.net.UnknownHostException", "TroupeTest", 3)

        Troupe.e(truncatedThrowable(ConnectException::class.java))
        assertExceptionLogged(Log.ERROR, null, "java.net.ConnectException", "TroupeTest", 4)

        Troupe.wtf(truncatedThrowable(AssertionError::class.java))
        assertExceptionLogged(Log.ASSERT, null, "java.lang.AssertionError", "TroupeTest", 5)
    }

    @Test
    fun exceptionOnlyCustomTag() {
        Troupe.recruit(AndroidDebugBard())

        Troupe.tag("Custom").v(truncatedThrowable(IllegalArgumentException::class.java))
        assertExceptionLogged(Log.VERBOSE, null, "java.lang.IllegalArgumentException", "Custom", 0)

        Troupe.tag("Custom").i(truncatedThrowable(NullPointerException::class.java))
        assertExceptionLogged(Log.INFO, null, "java.lang.NullPointerException", "Custom", 1)

        Troupe.tag("Custom").d(truncatedThrowable(UnsupportedOperationException::class.java))
        assertExceptionLogged(
            Log.DEBUG,
            null,
            "java.lang.UnsupportedOperationException",
            "Custom",
            2
        )

        Troupe.tag("Custom").w(truncatedThrowable(UnknownHostException::class.java))
        assertExceptionLogged(Log.WARN, null, "java.net.UnknownHostException", "Custom", 3)

        Troupe.tag("Custom").e(truncatedThrowable(ConnectException::class.java))
        assertExceptionLogged(Log.ERROR, null, "java.net.ConnectException", "Custom", 4)

        Troupe.tag("Custom").wtf(truncatedThrowable(AssertionError::class.java))
        assertExceptionLogged(Log.ASSERT, null, "java.lang.AssertionError", "Custom", 5)
    }

    @Test
    fun exceptionFromSpawnedThread() {
        Troupe.recruit(AndroidDebugBard())
        val datThrowable = truncatedThrowable(NullPointerException::class.java)
        val latch = CountDownLatch(1)
        object : Thread() {
            override fun run() {
                Troupe.e(datThrowable, "OMFG!")
                latch.countDown()
            }
        }.start()
        latch.await()
        assertExceptionLogged(
            Log.ERROR,
            "OMFG!",
            "java.lang.NullPointerException",
            "TroupeTest\$exceptionFro"
        )
    }

    @Test
    fun nullMessageWithThrowable() {
        Troupe.recruit(AndroidDebugBard())
        val datThrowable = truncatedThrowable(NullPointerException::class.java)
        Troupe.e(datThrowable, null)

        assertExceptionLogged(Log.ERROR, "", "java.lang.NullPointerException")
    }

    @Test
    fun chunkAcrossNewlinesAndLimit() {
        Troupe.recruit(AndroidDebugBard())
        Troupe.d(
            'a'.repeat(3000) + '\n'.toString() + 'b'.repeat(6000) + '\n'.toString() + 'c'.repeat(
                3000
            )
        )

        assertLog()
            .hasDebugMessage("TroupeTest", 'a'.repeat(3000))
            .hasDebugMessage("TroupeTest", 'b'.repeat(4000))
            .hasDebugMessage("TroupeTest", 'b'.repeat(2000))
            .hasDebugMessage("TroupeTest", 'c'.repeat(3000))
            .hasNoMoreMessages()
    }

    @Test
    fun nullMessageWithoutThrowable() {
        Troupe.recruit(AndroidDebugBard())
        Troupe.d(null as String?)

        assertLog().hasNoMoreMessages()
    }

    @Test
    fun logMessageCallback() {
        val logs = ArrayList<String>()
        Troupe.recruit(object : AndroidDebugBard() {
            override fun sing(priority: Int, tag: String?, message: String, throwable: Throwable?) {
                logs.add("$priority $tag $message")
            }
        })

        Troupe.v("Verbose")
        Troupe.tag("Custom").v("Verbose")
        Troupe.d("Debug")
        Troupe.tag("Custom").d("Debug")
        Troupe.i("Info")
        Troupe.tag("Custom").i("Info")
        Troupe.w("Warn")
        Troupe.tag("Custom").w("Warn")
        Troupe.e("Error")
        Troupe.tag("Custom").e("Error")
        Troupe.wtf("Assert")
        Troupe.tag("Custom").wtf("Assert")

        assertThat(logs).containsExactly( //
            "2 TroupeTest Verbose", //
            "2 Custom Verbose", //
            "3 TroupeTest Debug", //
            "3 Custom Debug", //
            "4 TroupeTest Info", //
            "4 Custom Info", //
            "5 TroupeTest Warn", //
            "5 Custom Warn", //
            "6 TroupeTest Error", //
            "6 Custom Error", //
            "7 TroupeTest Assert", //
            "7 Custom Assert" //
        )
    }

    @Test
    fun logAtSpecifiedPriority() {
        Troupe.recruit(AndroidDebugBard())

        Troupe.sing(Log.VERBOSE, "Hello, World!")
        Troupe.sing(Log.DEBUG, "Hello, World!")
        Troupe.sing(Log.INFO, "Hello, World!")
        Troupe.sing(Log.WARN, "Hello, World!")
        Troupe.sing(Log.ERROR, "Hello, World!")
        Troupe.sing(Log.ASSERT, "Hello, World!")

        assertLog()
            .hasVerboseMessage("TroupeTest", "Hello, World!")
            .hasDebugMessage("TroupeTest", "Hello, World!")
            .hasInfoMessage("TroupeTest", "Hello, World!")
            .hasWarnMessage("TroupeTest", "Hello, World!")
            .hasErrorMessage("TroupeTest", "Hello, World!")
            .hasAssertMessage("TroupeTest", "Hello, World!")
            .hasNoMoreMessages()
    }

    @Test
    fun isLoggableControlsLogging() {
        Troupe.recruit(object : AndroidDebugBard() {
            @Suppress("OverridingDeprecatedMember") // Explicitly testing deprecated variant.
            override fun isSingable(tag: String?, priority: Int): Boolean {
                return priority == Log.INFO
            }
        })
        Troupe.v("Hello, World!")
        Troupe.d("Hello, World!")
        Troupe.i("Hello, World!")
        Troupe.w("Hello, World!")
        Troupe.e("Hello, World!")
        Troupe.wtf("Hello, World!")

        assertLog()
            .hasInfoMessage("TroupeTest", "Hello, World!")
            .hasNoMoreMessages()
    }

    @Test
    fun isLoggableTagControlsLogging() {
        Troupe.recruit(object : AndroidDebugBard() {
            override fun isSingable(tag: String?, priority: Int): Boolean {
                return "FILTER" == tag
            }
        })
        Troupe.tag("FILTER").v("Hello, World!")
        Troupe.d("Hello, World!")
        Troupe.i("Hello, World!")
        Troupe.w("Hello, World!")
        Troupe.e("Hello, World!")
        Troupe.wtf("Hello, World!")

        assertLog()
            .hasVerboseMessage("FILTER", "Hello, World!")
            .hasNoMoreMessages()
    }

    @Test
    fun logsUnknownHostExceptions() {
        Troupe.recruit(AndroidDebugBard())
        Troupe.e(truncatedThrowable(UnknownHostException::class.java), null)

        assertExceptionLogged(Log.ERROR, "", "UnknownHostException")
    }

    @Test
    fun tagIsClearedWhenNotLoggable() {
        Troupe.recruit(object : AndroidDebugBard() {
            override fun isSingable(tag: String?, priority: Int): Boolean {
                return priority >= Log.WARN
            }
        })
        Troupe.tag("NotLogged").i("Message not logged")
        Troupe.w("Message logged")

        assertLog()
            .hasWarnMessage("TroupeTest", "Message logged")
            .hasNoMoreMessages()
    }

    private fun <T : Throwable> truncatedThrowable(throwableClass: Class<T>): T {
        val throwable = throwableClass.getConstructor().newInstance()
        val stackTrace = throwable.stackTrace
        val traceLength = if (stackTrace.size > 5) 5 else stackTrace.size
        throwable.stackTrace = stackTrace.copyOf(traceLength)
        return throwable
    }

    private fun Char.repeat(number: Int) = toString().repeat(number)

    private fun assertExceptionLogged(
        logType: Int,
        message: String?,
        exceptionClassname: String,
        tag: String? = null,
        index: Int = 0
    ) {
        val logs = getLogs()
        assertThat(logs).hasSize(index + 1)
        val log = logs[index]
        assertThat(log.type).isEqualTo(logType)
        assertThat(log.tag).isEqualTo(tag ?: "TroupeTest")

        if (message != null) {
            assertThat(log.msg).startsWith(message)
        }

        assertThat(log.msg).contains(exceptionClassname)
    }

    private fun assertLog(): LogAssert {
        return LogAssert(getLogs())
    }

    private fun getLogs() = ShadowLog.getLogs().filter { it.tag != ROBOLECTRIC_INSTRUMENTATION_TAG }

    private inline fun <reified T : Throwable> assertThrows(body: () -> Unit): ThrowableSubject {
        try {
            body()
        } catch (t: Throwable) {
            if (t is T) {
                return assertThat(t)
            }
            throw t
        }
        throw AssertionError("Expected body to throw ${T::class.java.name} but completed successfully")
    }

    private class LogAssert(private val items: List<ShadowLog.LogItem>) {
        private var index = 0

        fun hasVerboseMessage(tag: String, message: String): LogAssert {
            return hasMessage(Log.VERBOSE, tag, message)
        }

        fun hasDebugMessage(tag: String, message: String): LogAssert {
            return hasMessage(Log.DEBUG, tag, message)
        }

        fun hasInfoMessage(tag: String, message: String): LogAssert {
            return hasMessage(Log.INFO, tag, message)
        }

        fun hasWarnMessage(tag: String, message: String): LogAssert {
            return hasMessage(Log.WARN, tag, message)
        }

        fun hasErrorMessage(tag: String, message: String): LogAssert {
            return hasMessage(Log.ERROR, tag, message)
        }

        fun hasAssertMessage(tag: String, message: String): LogAssert {
            return hasMessage(Log.ASSERT, tag, message)
        }

        private fun hasMessage(priority: Int, tag: String, message: String): LogAssert {
            val item = items[index++]
            assertThat(item.type).isEqualTo(priority)
            assertThat(item.tag).isEqualTo(tag)
            assertThat(item.msg).isEqualTo(message)
            return this
        }

        fun hasNoMoreMessages() {
            assertThat(items).hasSize(index)
        }
    }

    private companion object {
        private const val ROBOLECTRIC_INSTRUMENTATION_TAG = "MonitoringInstr"
    }
}
