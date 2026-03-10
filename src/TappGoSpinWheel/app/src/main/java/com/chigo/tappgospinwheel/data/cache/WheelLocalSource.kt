package com.chigo.tappgospinwheel.data.cache

import android.content.Context
import android.content.SharedPreferences
import com.chigo.tappgospinwheel.util.Constants

class WheelLocalSource(context: Context) {

    private val appContext = context.applicationContext

    companion object {
        private const val PREFS_NAME      = "tappgo_spinwheel_prefs"
        private const val KEY_LAST_FETCH  = "last_config_fetch_time"
        private const val KEY_CONFIG_JSON = "cached_config_json"
        private const val KEY_LAST_RESULT = "last_spin_result"
        private const val KEY_SPIN_COUNT  = "total_spin_count"
        private const val PREF_LAST_SPIN_TIME = "last_spin_time"
    }

    private val prefs: SharedPreferences =
        appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // Config JSON

    fun saveConfigAndTimestamp(json: String) {
        prefs.edit()
            .putString(KEY_CONFIG_JSON, json)
            .putLong(KEY_LAST_FETCH, System.currentTimeMillis())
            .apply()
    }

    fun getConfig(): String? = prefs.getString(KEY_CONFIG_JSON, null)

    fun isCacheValid(): Boolean {
        val lastFetch = prefs.getLong(KEY_LAST_FETCH, 0L)
        if (lastFetch == 0L) return false
        return (System.currentTimeMillis() - lastFetch) < Constants.CACHE_EXPIRY_MS
    }

    // Spin state

    fun saveSpinResult(degrees: Float) {
        prefs.edit().putFloat(KEY_LAST_RESULT, degrees).apply()
    }

    @Synchronized
    fun incrementSpinCount() {
        val current = getSpinCount()
        prefs.edit().putInt(KEY_SPIN_COUNT, current + 1).apply()
    }

    fun getSpinCount(): Int = prefs.getInt(KEY_SPIN_COUNT, 0)

    // Helpers

    fun getLastSpinTime(): Long {
        return prefs.getLong(PREF_LAST_SPIN_TIME, 0L)
    }
    fun saveLastSpinTime(time: Long) {
        prefs.edit().putLong(PREF_LAST_SPIN_TIME, time).apply()
    }

    fun clearAll() = prefs.edit().clear().apply()
}