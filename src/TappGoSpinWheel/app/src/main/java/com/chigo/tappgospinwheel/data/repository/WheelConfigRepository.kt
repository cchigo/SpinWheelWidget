package com.chigo.tappgospinwheel.data.repository

import android.content.Context
import android.graphics.Bitmap
import com.chigo.tappgospinwheel.data.cache.WheelImageCache
import com.chigo.tappgospinwheel.data.cache.WheelLocalSource
import com.chigo.tappgospinwheel.data.network.WheelRemoteSource
import com.chigo.tappgospinwheel.model.BaseResponse
import com.chigo.tappgospinwheel.model.ImageKey
import com.chigo.tappgospinwheel.model.RotationConfig
import com.chigo.tappgospinwheel.model.WidgetConfig
import com.chigo.tappgospinwheel.model.WidgetConfigResponse
import com.chigo.tappgospinwheel.ui.WheelState
import com.chigo.tappgospinwheel.util.Constants
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class WheelConfigRepository(context: Context) {

    private val localSource = WheelLocalSource(context.applicationContext)
    private val remoteSource = WheelRemoteSource()
    private val imageCache = WheelImageCache(context.applicationContext)

    suspend fun getCachedState(): WheelState.Ready? {
        if (!localSource.isCacheValid()) return null
        val cached = localSource.getConfig() ?: return null
        return try {
            val config = Constants.json.decodeFromString(
                WidgetConfigResponse.serializer(), cached
            ).data.firstOrNull() ?: return null
            buildReadyState(config)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun fetchFreshConfig(): BaseResponse<WheelState.Ready> {
        return when (val result = syncConfig()) {
            is BaseResponse.Success -> {
                val config = result.data.data.firstOrNull()
                    ?: return BaseResponse.Error("No config found")
                imageCache.cleanup(buildActiveUrls(config))
                BaseResponse.Success(buildReadyState(config))
            }
            is BaseResponse.Error -> BaseResponse.Error(result.message, result.code)
            is BaseResponse.Loading -> BaseResponse.Loading
        }
    }

    suspend fun refreshIfExpired(): Boolean {
        if (localSource.isCacheValid()) return false
        return fetchFreshConfig() is BaseResponse.Success
    }

    fun loadWidgetBitmaps(): Map<ImageKey, Bitmap?> {
        val cached = localSource.getConfig() ?: return emptyMap()
        return try {
            val config = Constants.json.decodeFromString(
                WidgetConfigResponse.serializer(), cached
            ).data.firstOrNull() ?: return emptyMap()
            val host = config.network.assets.host
            mapOf(
                ImageKey.BG to imageCache.loadImage("$host${config.wheel.assets.bg}"),
                ImageKey.WHEEL to imageCache.loadImage("$host${config.wheel.assets.wheel}"),
                ImageKey.WHEEL_FRAME to imageCache.loadImage("$host${config.wheel.assets.wheelFrame}"),
                ImageKey.WHEEL_SPIN to imageCache.loadImage("$host${config.wheel.assets.wheelSpin}")
            )
        } catch (e: Exception) {
            emptyMap()
        }
    }

    fun getRotationConfig(): RotationConfig? {
        val cached = localSource.getConfig() ?: return null
        return try {
            Constants.json.decodeFromString(
                WidgetConfigResponse.serializer(), cached
            ).data.firstOrNull()?.wheel?.rotation
        } catch (e: Exception) {
            null
        }
    }

    fun getRefreshInterval(): Int {
        val cached = localSource.getConfig() ?: return 300
        return try {
            Constants.json.decodeFromString(
                WidgetConfigResponse.serializer(), cached
            ).data.firstOrNull()?.network?.attributes?.refreshInterval ?: 300
        } catch (e: Exception) {
            300
        }
    }

    private suspend fun syncConfig(): BaseResponse<WidgetConfigResponse> {
        return when (val result = remoteSource.fetchConfig(Constants.CONFIG_URL)) {
            is BaseResponse.Success -> {
                localSource.saveConfigAndTimestamp(
                    Constants.json.encodeToString(WidgetConfigResponse.serializer(), result.data)
                )
                BaseResponse.Success(result.data)
            }
            is BaseResponse.Error -> loadStaleConfig() ?: BaseResponse.Error(result.message, result.code)
            is BaseResponse.Loading -> BaseResponse.Loading
        }
    }

    private fun loadStaleConfig(): BaseResponse.Success<WidgetConfigResponse>? {
        val stale = localSource.getConfig() ?: return null
        return try {
            BaseResponse.Success(
                Constants.json.decodeFromString(WidgetConfigResponse.serializer(), stale)
            )
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun buildReadyState(config: WidgetConfig): WheelState.Ready {
        val images = coroutineScope {
            val bg = async { fetchBitmap("${config.network.assets.host}${config.wheel.assets.bg}") }
            val wheel =
                async { fetchBitmap("${config.network.assets.host}${config.wheel.assets.wheel}") }
            val frame =
                async { fetchBitmap("${config.network.assets.host}${config.wheel.assets.wheelFrame}") }
            val spin =
                async { fetchBitmap("${config.network.assets.host}${config.wheel.assets.wheelSpin}") }
            mapOf(
                ImageKey.BG to bg.await(),
                ImageKey.WHEEL to wheel.await(),
                ImageKey.WHEEL_FRAME to frame.await(),
                ImageKey.WHEEL_SPIN to spin.await()
            )
        }
        return WheelState.Ready(
            bg             = images[ImageKey.BG],
            wheel          = images[ImageKey.WHEEL],
            wheelFrame     = images[ImageKey.WHEEL_FRAME],
            wheelSpin      = images[ImageKey.WHEEL_SPIN],
            rotationConfig = config.wheel.rotation
        )
    }

    private suspend fun fetchBitmap(url: String): Bitmap? {
        imageCache.loadImage(url)?.let { return it }
        return when (val result = remoteSource.downloadImageBytes(url)) {
            is BaseResponse.Success -> {
                imageCache.saveImage(url, result.data)
                imageCache.loadImage(url)
            }
            else -> null
        }
    }

    private fun buildActiveUrls(config: WidgetConfig): Set<String> {
        val host = config.network.assets.host
        return setOf(
            "$host${config.wheel.assets.bg}",
            "$host${config.wheel.assets.wheel}",
            "$host${config.wheel.assets.wheelFrame}",
            "$host${config.wheel.assets.wheelSpin}"
        )
    }
}