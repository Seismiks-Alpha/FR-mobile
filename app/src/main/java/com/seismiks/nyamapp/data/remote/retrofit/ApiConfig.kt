package com.seismiks.nyamapp.data.remote.retrofit

import com.seismiks.nyamapp.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class ApiConfig {
    companion object {
        fun getApiService(): ApiService {
            val loggingInterceptor =
                HttpLoggingInterceptor().apply {
                    level = if (BuildConfig.DEBUG) {
                        HttpLoggingInterceptor.Level.BODY
                    } else {
                        HttpLoggingInterceptor.Level.NONE
                    }
                }
                    .setLevel(HttpLoggingInterceptor.Level.BODY)
            val client = OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .connectTimeout(30, TimeUnit.SECONDS) // Batas waktu koneksi
                .readTimeout(60, TimeUnit.SECONDS)    // Batas waktu membaca balasan (paling penting untuk upload)
                .writeTimeout(60, TimeUnit.SECONDS)   // Batas waktu mengirim data
                .build()
            val retrofit = Retrofit.Builder()
                .baseUrl("https://api.seix.me/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()
            return retrofit.create(ApiService::class.java)
        }
    }
}