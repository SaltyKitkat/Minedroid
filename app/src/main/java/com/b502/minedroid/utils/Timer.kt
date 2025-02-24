package com.b502.minedroid.utils

import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.ZERO
import kotlin.time.TimeSource.Monotonic.ValueTimeMark
import kotlin.time.TimeSource.Monotonic.markNow

class Timer(
    private val handler: (Duration) -> Unit, private val interval: Duration //单位1ms
) {
    private var passedTime = ZERO
    private var startTime: ValueTimeMark? = null
    private var ticker = Ticker(interval / 10) { onTick() }

    fun pause() {
        if (startTime != null) {
            ticker.stop()
            val dur = markNow() - startTime!!
            passedTime = dur + passedTime
            handler(passedTime)
        }
    }

    fun resume() {
        if (startTime != null) {
            ticker.start()
            startTime = markNow()
            handler(passedTime)
        }
    }

    fun stop(): Duration {
        ticker.stop()
        if (startTime != null) {
            val dur = markNow() - startTime!!
            val ret = dur + passedTime
            handler(ret)
            passedTime = ZERO
            startTime = null
            return ret
        }
        return ZERO
    }

    fun start() {
        startTime = markNow()
        ticker.start()
    }

    private fun onTick() {
        if (startTime != null) {
            val cur = markNow()
            val diff = cur - startTime!!
            if (diff >= interval) {
                startTime = cur
                passedTime += diff
                handler(passedTime)
            }
        }
    }


    @Throws(Throwable::class)
    protected fun finalize() {
        stop()
    }
}

private class Ticker(val interval: Duration, private val onTick: () -> Unit) {
    private var job: Job? = null
    private var running = false
    fun start() {
        running = true
        job = MainScope().launch {
            while (running) {
                delay(interval)
                onTick()
            }
        }
    }

    fun stop() {
        running = false
        job = null
    }
}