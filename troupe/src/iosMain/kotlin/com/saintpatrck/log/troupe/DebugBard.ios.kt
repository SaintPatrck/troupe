package com.saintpatrck.log.troupe

actual open class DebugBard : Troupe.Bard() {
    override fun sing(priority: Int, tag: String?, message: String, throwable: Throwable?) {

    }
}