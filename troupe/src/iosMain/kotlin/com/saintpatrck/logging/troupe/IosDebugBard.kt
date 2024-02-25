package com.saintpatrck.logging.troupe

import platform.Foundation.NSLog

open class IosDebugBard : Troupe.Bard() {
    override fun sing(priority: Int, tag: String?, message: String, throwable: Throwable?) {
        NSLog(message)
    }
}