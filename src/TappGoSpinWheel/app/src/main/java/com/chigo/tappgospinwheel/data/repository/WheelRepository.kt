package com.chigo.tappgospinwheel.data.repository

import android.content.Context
import android.graphics.Bitmap
import com.chigo.tappgospinwheel.data.repository.WheelSpinRepository
import com.chigo.tappgospinwheel.model.BaseResponse
import com.chigo.tappgospinwheel.model.ImageKey
import com.chigo.tappgospinwheel.model.RotationConfig
import com.chigo.tappgospinwheel.ui.WheelState

class WheelRepository(context: Context) {

    private val configRepo = WheelConfigRepository(context)
    private val spinRepo = WheelSpinRepository(context)

    // Config
    suspend fun getCachedState(): WheelState.Ready? = configRepo.getCachedState()
    suspend fun fetchFreshConfig(): BaseResponse<WheelState.Ready> = configRepo.fetchFreshConfig()
    suspend fun refreshIfExpired(): Boolean = configRepo.refreshIfExpired()
    fun loadWidgetBitmaps(): Map<ImageKey, Bitmap?> = configRepo.loadWidgetBitmaps()
    fun getRotationConfig(): RotationConfig? = configRepo.getRotationConfig()

    // Spin
    fun isCooldownActive(): Boolean = spinRepo.isCooldownActive(configRepo.getRefreshInterval())
    fun getTimeUntilNextSpin(): Long = spinRepo.getTimeUntilNextSpin(configRepo.getRefreshInterval())
    fun getFormattedTimeUntilNextSpin(): String = spinRepo.getFormattedTimeUntilNextSpin(configRepo.getRefreshInterval())
    fun getLastSpinTime(): Long = spinRepo.getLastSpinTime()
    fun saveLastSpinTime() = spinRepo.saveLastSpinTime()
    fun saveSpinResult(degrees: Float) = spinRepo.saveSpinResult(degrees)
    fun incrementSpinCount() = spinRepo.incrementSpinCount()
    fun getSpinCount(): Int = spinRepo.getSpinCount()
}