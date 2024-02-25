@file:JvmName("Troupe")

package com.saintpatrck.logging.troupe

import kotlin.concurrent.Volatile
import kotlin.jvm.JvmName
import kotlin.jvm.JvmStatic
import kotlin.jvm.JvmSynthetic
import kotlin.native.concurrent.ThreadLocal

class Troupe private constructor() {

    init {
        throw IllegalStateException()
    }

    /**
     * A facade for handling logging calls. Install instances via [Troupe.recruit].
     */
    abstract class Bard {
        @Volatile
        @get:JvmSynthetic
        internal var explicitTag: String? = null

        @get:JvmSynthetic
        internal open val tag: String?
            get() {
                val tag = explicitTag
                if (explicitTag != null) {
                    explicitTag = null
                }
                return tag
            }

        /**
         * Sing a verbose message.
         */
        open fun v(message: String?) {
            composeStanza(priority = Priority.VERBOSE, message = message)
        }

        /**
         * Sing of a verbose exception and message.
         */
        open fun v(t: Throwable?, message: String?) {
            composeStanza(priority = Priority.VERBOSE, throwable = t, message = message)
        }

        /**
         * Sing of a verbose exception.
         */
        open fun v(t: Throwable?) {
            composeStanza(priority = Priority.VERBOSE, throwable = t)
        }

        /**
         * Sing of a debug message.
         */
        open fun d(message: String?) {
            composeStanza(priority = Priority.DEBUG, message = message)
        }

        /**
         * Sing of a debug exception and message.
         */
        open fun d(throwable: Throwable?, message: String?) {
            composeStanza(priority = Priority.DEBUG, throwable = throwable, message = message)
        }

        /**
         * Sing of a debug exception.
         */
        open fun d(throwable: Throwable?) {
            composeStanza(priority = Priority.DEBUG, throwable = throwable)
        }

        /**
         * Sing of an informational message.
         */
        open fun i(message: String?) {
            composeStanza(priority = Priority.INFO, message = message)
        }

        /**
         * Sing of an informational exception and message.
         */
        open fun i(throwable: Throwable?, message: String?) {
            composeStanza(priority = Priority.INFO, throwable = throwable, message = message)
        }

        /**
         * Sing of an informational exception.
         */
        open fun i(throwable: Throwable?) {
            composeStanza(priority = Priority.INFO, throwable = throwable)
        }

        /**
         * Sing of a warning message.
         */
        open fun w(message: String?) {
            composeStanza(priority = Priority.WARN, message = message)
        }

        /**
         * Sing of a warning exception and message.
         */
        open fun w(throwable: Throwable?, message: String?) {
            composeStanza(priority = Priority.WARN, throwable = throwable, message = message)
        }

        /**
         * Sing of a warning exception.
         */
        open fun w(throwable: Throwable?) {
            composeStanza(priority = Priority.WARN, throwable = throwable)
        }

        /**
         * Sing of an error message.
         */
        open fun e(message: String?) {
            composeStanza(priority = Priority.ERROR, message = message)
        }

        /**
         * Sing of an error exception and message.
         */
        open fun e(throwable: Throwable?, message: String?) {
            composeStanza(priority = Priority.ERROR, throwable = throwable, message = message)
        }

        /**
         * Sing of an error exception.
         */
        open fun e(throwable: Throwable?) {
            composeStanza(priority = Priority.ERROR, throwable = throwable)
        }

        /**
         * Sing of an assert message.
         */
        open fun wtf(message: String?) {
            composeStanza(priority = Priority.ASSERT, message = message)
        }

        /**
         * Sing of an assert exception and message.
         */
        open fun wtf(throwable: Throwable?, message: String?) {
            composeStanza(priority = Priority.ASSERT, throwable = throwable, message = message)
        }

        /**
         * Sing of an assert exception.
         */
        open fun wtf(throwable: Throwable?) {
            composeStanza(priority = Priority.ASSERT, throwable = throwable)
        }

        /**
         * Sing of a message at [priority].
         */
        open fun sing(priority: Int, message: String?) {
            composeStanza(priority = priority, message = message)
        }

        /**
         * Sing of an exception and message at [priority].
         */
        open fun sing(priority: Int, throwable: Throwable?, message: String?) {
            composeStanza(priority = priority, throwable = throwable, message = message)
        }

        /**
         * Sing of an exception at [priority].
         */
        open fun sing(priority: Int, throwable: Throwable?) {
            composeStanza(priority = priority, throwable = throwable)
        }

        /**
         * Return whether a message with [tag] or [priority] should be composed into song.
         */
        protected open fun isSingable(tag: String?, priority: Int) = true

        private fun composeStanza(
            priority: Int,
            throwable: Throwable? = null,
            message: String? = null
        ) {
            // Consume tag even when message is not loggable so that next message is correctly tagged.
            val tag = tag
            if (!isSingable(tag, priority)) {
                return
            }

            var msg = message
            if (msg.isNullOrEmpty()) {
                if (throwable == null) {
                    // Swallow message if it is null and there is no throwable
                    return
                }
                msg = throwable.stackTraceToString()
            } else {
                if (throwable != null) {
                    msg += "\n" + throwable.stackTraceToString()
                }
            }

            sing(priority, tag, msg, throwable)
        }

        protected abstract fun sing(
            priority: Int,
            tag: String?,
            message: String,
            throwable: Throwable?
        )
    }

    object Priority {
        const val VERBOSE = 2
        const val DEBUG = 3
        const val INFO = 4
        const val WARN = 5
        const val ERROR = 6
        const val ASSERT = 7
    }

    @ThreadLocal
    companion object : Bard() {

        @JvmStatic
        override fun v(message: String?) {
            bardArray.forEach { it.v(message) }
        }

        @JvmStatic
        override fun v(t: Throwable?, message: String?) {
            bardArray.forEach { it.v(t, message) }
        }

        @JvmStatic
        override fun v(t: Throwable?) {
            bardArray.forEach { it.v(t) }
        }

        @JvmStatic
        override fun d(message: String?) {
            bardArray.forEach { it.d(message) }
        }

        @JvmStatic
        override fun d(throwable: Throwable?, message: String?) {
            bardArray.forEach { it.d(throwable, message) }
        }

        @JvmStatic
        override fun d(throwable: Throwable?) {
            bardArray.forEach { it.d(throwable) }
        }

        @JvmStatic
        override fun i(message: String?) {
            bardArray.forEach { it.i(message) }
        }

        @JvmStatic
        override fun i(throwable: Throwable?, message: String?) {
            bardArray.forEach { it.i(throwable, message) }
        }

        @JvmStatic
        override fun i(throwable: Throwable?) {
            bardArray.forEach { it.i(throwable) }
        }

        @JvmStatic
        override fun w(message: String?) {
            bardArray.forEach { it.w(message) }
        }

        @JvmStatic
        override fun w(throwable: Throwable?, message: String?) {
            bardArray.forEach { it.w(throwable, message) }
        }

        @JvmStatic
        override fun w(throwable: Throwable?) {
            bardArray.forEach { it.w(throwable) }
        }

        @JvmStatic
        override fun e(message: String?) {
            bardArray.forEach { it.e(message) }
        }

        @JvmStatic
        override fun e(throwable: Throwable?, message: String?) {
            bardArray.forEach { it.e(throwable, message) }
        }

        @JvmStatic
        override fun e(throwable: Throwable?) {
            bardArray.forEach { it.e(throwable) }
        }

        @JvmStatic
        override fun wtf(message: String?) {
            bardArray.forEach { it.wtf(message) }
        }

        @JvmStatic
        override fun wtf(throwable: Throwable?, message: String?) {
            bardArray.forEach { it.wtf(throwable, message) }
        }

        @JvmStatic
        override fun wtf(throwable: Throwable?) {
            bardArray.forEach { it.wtf(throwable) }
        }

        @JvmStatic
        override fun sing(priority: Int, message: String?) {
            bardArray.forEach { it.sing(priority, message) }
        }

        @JvmStatic
        override fun sing(priority: Int, throwable: Throwable?, message: String?) {
            bardArray.forEach { it.sing(priority, throwable, message) }
        }

        @JvmStatic
        override fun sing(priority: Int, throwable: Throwable?) {
            bardArray.forEach { it.sing(priority, throwable) }
        }

        override fun sing(priority: Int, tag: String?, message: String, throwable: Throwable?) {
            throw IllegalStateException()
        }

        /**
         * A view into Krier's registered subscribers as a Krier itself. This can be used for
         * injecting a logger instance rather than using static methods or to facilitate testing.
         */
        @Suppress(
            "NON_FINAL_MEMBER_IN_OBJECT" // For japicmp check.
        )
        @JvmStatic
        open inline fun asBard(): Bard = this

        /** Set a one-time tag for use on the next logging call. */
        @JvmStatic
        fun tag(tag: String): Bard {
            for (subscriber in bardArray) {
                subscriber.explicitTag = tag
            }
            return this
        }

        /** Recruit a new [bard] to our Troupe. */
        @JvmStatic
        fun recruit(bard: Bard) {
            require(bard !== this) { "Cannot recruit Troupe into itself." }
            bards.add(bard)
            bardArray = bards.toTypedArray()
        }

        /** Recruit new [bards] to our Troupe. */
        @JvmStatic
        fun recruit(vararg bards: Bard) {
            for (subscriber in bards) {
                require(subscriber !== this) { "Cannot recruit Troupe into itself." }
            }

            Companion.bards.addAll(bards)
            Companion.bards.addAll(bards)
            bardArray = Companion.bards.toTypedArray()
        }

        /** Disband a recruited [bard]. */
        @JvmStatic
        fun disband(bard: Bard) {
            require(bards.remove(bard)) { "Cannot disband bard which is not recruited: $bard" }
            bardArray = bards.toTypedArray()
        }

        /**
         * Disband all recruited [bards]. */
        @JvmStatic
        fun disbandAll() {
            bards.clear()
            bardArray = emptyArray()
        }

        /** Return a copy of all recruited [Bard]s. */
        @JvmStatic
        fun dramatisPersonae(): List<Bard> {
            return bards.toList()
        }

        @get:[JvmStatic JvmName("subscriberCount")]
        val bardCount get() = bardArray.size

        private val bards = ArrayList<Bard>()

        @Volatile
        private var bardArray = emptyArray<Bard>()
    }
}

fun sing(priority: Int, message: String?) {
    Troupe.sing(priority, message)
}

fun sing(tag: String, priority: Int, message: String?) {
    Troupe.tag(tag).sing(priority, message)
}
