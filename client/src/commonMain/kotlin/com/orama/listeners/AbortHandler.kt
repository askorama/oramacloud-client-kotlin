package com.orama.listeners

class AbortHandler {
    private var abortCallback: (() -> Unit)? = null

    fun setInterruptCallback(callback: () -> Unit) {
        abortCallback = callback
    }

    fun abort() {
        abortCallback?.invoke()
    }
}