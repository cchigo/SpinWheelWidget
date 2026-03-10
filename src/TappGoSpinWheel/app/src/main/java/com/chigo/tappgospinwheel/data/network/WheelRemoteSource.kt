package com.chigo.tappgospinwheel.data.network

import android.content.Context
import com.chigo.tappgospinwheel.model.BaseResponse
import com.chigo.tappgospinwheel.model.WidgetConfigResponse
import com.chigo.tappgospinwheel.util.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WheelRemoteSource() {
    private val apiService: WheelApiService = WheelApiServiceImpl()

    suspend fun fetchConfig(url: String): BaseResponse<WidgetConfigResponse> {
        return when (val result = apiService.fetchConfig(url)) {
            is BaseResponse.Success -> {
                try {
                    val parsed = Constants.json.decodeFromString(
                        WidgetConfigResponse.serializer(),
                        result.data
                    )
                    BaseResponse.Success(parsed)
                } catch (e: Exception) {
                    BaseResponse.Error(e.message ?: "Failed to parse config")
                }
            }
            is BaseResponse.Error -> BaseResponse.Error(result.message, result.code)
            is BaseResponse.Loading -> BaseResponse.Loading
        }
    }

    suspend fun downloadImageBytes(url: String): BaseResponse<ByteArray> =
        withContext(Dispatchers.IO) {
            when (val result = apiService.downloadImage(url)) {
                is BaseResponse.Success -> BaseResponse.Success(result.data)
                is BaseResponse.Error -> BaseResponse.Error(result.message, result.code)
                is BaseResponse.Loading -> BaseResponse.Loading
            }
        }
}