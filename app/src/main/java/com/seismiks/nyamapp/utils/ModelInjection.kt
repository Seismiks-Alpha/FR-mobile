package com.seismiks.nyamapp.utils

import android.content.Context
import com.seismiks.nyamapp.data.local.ResultRepository
import com.seismiks.nyamapp.data.remote.RemoteRepository
import com.seismiks.nyamapp.data.remote.retrofit.ApiConfig

object ModelInjection {
    fun provideRepository(context: Context): Pair<ResultRepository, RemoteRepository> {
        val apiService = ApiConfig.getApiService()

        val resultRepository = ResultRepository.getInstance(context)
        val remoteRepository = RemoteRepository.getInstance(apiService)
        return Pair(resultRepository, remoteRepository)
    }
}