package com.chigo.tappgospinwheel.data.network

import com.chigo.tappgospinwheel.model.BaseResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request

class WheelApiServiceImpl : WheelApiService {

    private val client = NetworkModule.okHttpClient

    override suspend fun fetchConfig(url: String): BaseResponse<String> =
        withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder().url(url).build()
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val body = response.body?.string()
                        ?: return@withContext BaseResponse.Error("Empty response body")
                    BaseResponse.Success(body)
                } else {
                    BaseResponse.Error("HTTP error", response.code)
                }
            } catch (e: Exception) {
                BaseResponse.Error(e.message ?: "Unknown error")
            }
        }

    override suspend fun downloadImage(url: String): BaseResponse<ByteArray> =
        withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder().url(url).build()
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val bytes = response.body?.bytes()
                        ?: return@withContext BaseResponse.Error("Empty image response")
                    BaseResponse.Success(bytes)
                } else {
                    BaseResponse.Error("HTTP error", response.code)
                }
            } catch (e: Exception) {
                BaseResponse.Error(e.message ?: "Unknown error")
            }
        }
}