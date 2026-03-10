package com.chigo.tappgospinwheel.data.network

import com.chigo.tappgospinwheel.model.BaseResponse
import okhttp3.Request



interface WheelApiService {
    suspend fun fetchConfig(url: String): BaseResponse<String>
    suspend fun downloadImage(url: String): BaseResponse<ByteArray>
}

