package com.reza.nyamapp.utils

import android.content.Context
import com.reza.nyamapp.data.local.ResultRepository
import com.reza.nyamapp.data.remote.RemoteRepository
import com.reza.nyamapp.data.remote.retrofit.ApiConfig

object ModelInjection {
    fun provideRepository(context: Context): Pair<ResultRepository, RemoteRepository> {
        val apiService = ApiConfig.getApiService()

        val resultRepository = ResultRepository.getInstance(context)
        val remoteRepository = RemoteRepository.getInstance(apiService)
        return Pair(resultRepository, remoteRepository)
    }
}