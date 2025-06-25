package com.marcoshier.services

import kotlin.concurrent.thread

class RefreshService(val callbacks: MutableList<() -> Unit> = mutableListOf()) {

    var start = System.currentTimeMillis()

    var t = 0
        set(value) {
            if (field != value) {
                if (field % 60 == 0) {
                    for (callback in callbacks) {
                        callback()
                    }
                }

                field = value
            }
        }

    fun add(f: () -> Unit) {
        callbacks.add(f)
    }

    fun tick() {
        start = System.currentTimeMillis()

        while (true) {
            t = ((System.currentTimeMillis() - start) / 1000.0).toInt()
        }
    }

}