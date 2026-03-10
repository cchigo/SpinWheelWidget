package com.chigo.tappgospinwheel.util

import kotlinx.serialization.json.Json

object Constants {
    const val CONFIG_URL =
        "https://drive.google.com/uc?export=download&id=1_mb2fSRuylnb-wy9EC104uXXv_QehgH0"
    const val API_TIMEOUT = 30L

    const val CACHE_EXPIRY_MS = 15 * 60 * 1000L // 15 minutes

    val json = Json { ignoreUnknownKeys = true; isLenient = true }

    const val ACTION_SPIN = "com.chigo.tappgospinwheel.ACTION_SPIN"
}