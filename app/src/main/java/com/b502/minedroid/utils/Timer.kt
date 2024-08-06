package com.b502.minedroid.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class Timer(
    private var handler: () -> Unit, //单位0.1s
    private var interval: Int
) {
    var isHangedup: Boolean = false
    private var isFinished: Boolean = false
    private var job: Job? = null

    fun pause() {
        isHangedup = true
    }

    fun stop() {
        isFinished = true
        isHangedup = false
        runBlocking { launch { job?.cancel() } }
    }

    fun start() {   
        if (isHangedup) {
            isHangedup = false
        }
        isFinished = false
        job = CoroutineScope(Dispatchers.Main).launch {
            var ticker = 0
            while (!isFinished && !isHangedup) {
                if (ticker >= interval) {
                    ticker = 0
                    handler()
                }
                delay(100)
                ticker++
            }
        }
        job!!.start()
    }

    @Throws(Throwable::class)
    protected fun finalize() {
        stop()
    }
}
