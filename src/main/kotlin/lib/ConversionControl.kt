package com.marcoshier.lib

import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

class ConversionControl {
    private val cancelled = AtomicBoolean(false)
    private val current = AtomicReference<Process?>(null)

    val isCancelled: Boolean get() = cancelled.get()

    fun attach(process: Process) {
        current.set(process)
        if (cancelled.get()) process.destroyForcibly()
    }

    fun detach(process: Process) {
        current.compareAndSet(process, null)
    }

    fun cancel() {
        cancelled.set(true)
        current.getAndSet(null)?.destroyForcibly()
    }
}