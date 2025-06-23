package com.marcoshier.services

import kotlin.concurrent.thread

class RefreshService(val callback: () -> Unit) {

    var start = System.currentTimeMillis()

    var t = 0
        set(value) {
            if (field != value) {
                if (field % 60 == 0) {
                    callback()
                }

                field = value
            }
        }


    fun tick() {
        start = System.currentTimeMillis()

        while (true) {
            t = ((System.currentTimeMillis() - start) / 1000.0).toInt()
        }
    }

}