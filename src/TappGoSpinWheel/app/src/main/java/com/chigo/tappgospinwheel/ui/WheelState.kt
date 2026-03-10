package com.chigo.tappgospinwheel.ui

import android.graphics.Bitmap
import com.chigo.tappgospinwheel.model.RotationConfig

sealed class WheelState {

    object Loading : WheelState()

    data class Ready(
        val bg: Bitmap?,
        val wheel: Bitmap?,
        val wheelFrame: Bitmap?,
        val wheelSpin: Bitmap?,
        val rotationConfig: RotationConfig
    ) : WheelState()

    data class Error(val message: String) : WheelState()
}

sealed class CooldownState {
    object Ready : CooldownState()
    data class Active(val timeLeft: Long) : CooldownState()
}