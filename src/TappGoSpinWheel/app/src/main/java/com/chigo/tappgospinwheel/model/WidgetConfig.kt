package com.chigo.tappgospinwheel.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WidgetConfigResponse(
    val data: List<WidgetConfig>,
    val meta: Meta
)

@Serializable
data class Meta(
    val version: Int,
    val copyright: String
)

@Serializable
data class WidgetConfig(
    val id: String,
    val name: String,
    val type: String,
    val network: NetworkConfig,
    val wheel: WheelConfig
)

@Serializable
data class NetworkConfig(
    val attributes: NetworkAttributes,
    val assets: NetworkAssets
)

@Serializable
data class NetworkAttributes(
    val refreshInterval: Int = 300,
    val networkTimeout: Int = 30000,
    val retryAttempts: Int = 3,
    val cacheExpiration: Int = 3600,
    val debugMode: Boolean = false
)

@Serializable
data class NetworkAssets(
    val host: String
)

@Serializable
data class WheelConfig(
    val rotation: RotationConfig,
    val assets: WheelAssets
)

@Serializable
data class RotationConfig(
    val duration: Int = 2000,
    val minimumSpins: Int = 3,
    val maximumSpins: Int = 5,
    val spinEasing: String = "easeInOutCubic"
)

@Serializable
data class WheelAssets(
    val bg: String,
    val wheelFrame: String,
    val wheelSpin: String,
    val wheel: String
)
