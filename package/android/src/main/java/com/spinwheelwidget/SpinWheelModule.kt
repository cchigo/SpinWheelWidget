package com.spinwheelwidget

import android.content.Intent
import com.chigo.tappgospinwheel.data.repository.WheelRepository
import com.facebook.react.bridge.Callback
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.module.annotations.ReactModule

@ReactModule(name = SpinWheelModule.NAME)
class SpinWheelModule(private val reactContext: ReactApplicationContext) :
    ReactContextBaseJavaModule(reactContext) {

    companion object {
        const val NAME = "SpinWheelModule"
    }

    override fun getName(): String = NAME

    @ReactMethod
    fun show() {
        val activity = reactContext.currentActivity ?: return
        val intent = Intent(activity, com.chigo.tappgospinwheel.MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        reactContext.startActivity(intent)
    }

    @ReactMethod
    fun hide() {
        val activity = reactContext.currentActivity ?: return
        if (activity is com.chigo.tappgospinwheel.MainActivity) {
            activity.finish()
        }
    }

    @ReactMethod
    fun spin(onComplete: Callback?) {
        show()
    }

    @ReactMethod
    fun getSpinCount(promise: Promise) {
        try {
            val repo = WheelRepository(reactContext)
            promise.resolve(repo.getSpinCount())
        } catch (e: Exception) {
            promise.reject("ERR_SPIN_COUNT", e.message)
        }
    }

    @ReactMethod
    fun getCooldownTimeLeft(promise: Promise) {
        try {
            val repo = WheelRepository(reactContext)
            promise.resolve(repo.getTimeUntilNextSpin().toDouble())
        } catch (e: Exception) {
            promise.reject("ERR_COOLDOWN", e.message)
        }
    }

    @ReactMethod
    fun getCooldownFormatted(promise: Promise) {
        try {
            val repo = WheelRepository(reactContext)
            promise.resolve(repo.getFormattedTimeUntilNextSpin())
        } catch (e: Exception) {
            promise.reject("ERR_COOLDOWN_FMT", e.message)
        }
    }
}