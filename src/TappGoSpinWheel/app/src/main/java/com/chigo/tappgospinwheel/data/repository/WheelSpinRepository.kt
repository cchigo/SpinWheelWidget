package com.chigo.tappgospinwheel.data.repository

import android.content.Context
import com.chigo.tappgospinwheel.data.cache.WheelLocalSource

class WheelSpinRepository(context: Context) {

    private val localSource = WheelLocalSource(context.applicationContext)

    fun isCooldownActive(refreshInterval: Int): Boolean {
        val lastSpin = localSource.getLastSpinTime()
        if (lastSpin == 0L) return false
        return (System.currentTimeMillis() - lastSpin) < refreshInterval * 1000L
    }

    fun getTimeUntilNextSpin(refreshInterval: Int): Long {
        val lastSpin = localSource.getLastSpinTime()
        return ((lastSpin + refreshInterval * 1000L) - System.currentTimeMillis()).coerceAtLeast(0L)
    }

    fun getFormattedTimeUntilNextSpin(refreshInterval: Int): String {
        val timeLeft = getTimeUntilNextSpin(refreshInterval)
        val minutes = (timeLeft / 1000) / 60
        val seconds = (timeLeft / 1000) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    fun saveLastSpinTime() = localSource.saveLastSpinTime(System.currentTimeMillis())
    fun getLastSpinTime(): Long = localSource.getLastSpinTime()
    fun saveSpinResult(degrees: Float) = localSource.saveSpinResult(degrees)
    fun incrementSpinCount() = localSource.incrementSpinCount()
    fun getSpinCount(): Int = localSource.getSpinCount()
}