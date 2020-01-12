package com.example.chess.utils

import java.util.*

class ReusableTimer(
    private val delay: Long,
    private val period: Long,
    private val action: () -> Unit
) {
    private val task
        get() = object : TimerTask() {
            override fun run() {
                action.invoke()
            }
        }

    @Volatile
    private var timer: Timer? = null

    @Synchronized
    fun schedule() {
        timer?.let { stop() }
        timer = Timer()
        timer?.schedule(task, delay, period)
    }

    @Synchronized
    fun scheduleAtFixedRate() {
        timer?.let { stop() }
        timer = Timer()
        timer?.scheduleAtFixedRate(task, delay, period)
    }

    @Synchronized
    fun stop() {
        timer?.cancel()
        timer = null
    }
}