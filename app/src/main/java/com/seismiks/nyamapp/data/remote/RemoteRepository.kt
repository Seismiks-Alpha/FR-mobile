package com.seismiks.nyamapp.data.remote

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.seismiks.nyamapp.data.Result
import com.seismiks.nyamapp.data.remote.response.ChatResponse
import com.seismiks.nyamapp.data.remote.response.ProfileResponse
import com.seismiks.nyamapp.data.remote.response.ResultsItem
import com.seismiks.nyamapp.data.remote.response.SyncProfileResponse
import com.seismiks.nyamapp.data.remote.retrofit.ApiService
import com.seismiks.nyamapp.data.remote.retrofit.Photo
import com.seismiks.nyamapp.data.remote.retrofit.ProfileUser
import com.seismiks.nyamapp.data.remote.retrofit.chat.PostChat
import okhttp3.MultipartBody

class RemoteRepository private constructor(private val apiService: ApiService) {
    suspend fun postQuestion(
        token: String,
        userId: String,
        question: String
    ): LiveData<Result<ChatResponse>> =
        liveData {
            emit(Result.Loading)
            try {
                val userQuestion = PostChat(userId, question)
                val response = apiService.postQuestion("Bearer $token", userQuestion)
                emit(Result.Success(response))
                Log.d(TAG, "postQuestion: $response")
            } catch (e: Exception) {
                emit(Result.Error(e.message.toString()))
                Log.d(TAG, "postQuestion: ${e.message.toString()}")
            }
        }

    suspend fun syncProfile(token: String): Result<SyncProfileResponse> {
        return try {
            val authHeader = "Bearer $token"
            val response = apiService.syncProfile(authHeader)
            Result.Success(response)
        } catch (e: Exception) {
            val errorMessage = e.message ?: "Unknown error during syncProfile"
            Log.e(TAG, "syncProfile Error: $errorMessage", e)
            Result.Error(errorMessage)
        }
    }

    fun getProfile(token: String): LiveData<Result<ProfileResponse>> = liveData {
        emit(Result.Loading)
        try {
            val authHeader = "Bearer $token"
            val response = apiService.getProfile(authHeader)
            emit(Result.Success(response))
            Log.d(TAG, "getProfile: $response")
        } catch (e: Exception) {
            emit(Result.Error(e.message.toString()))
            Log.d(TAG, "getProfile: ${e.message.toString()}")
        }
    }

    fun putProfile(token: String, profile: ProfileUser): LiveData<Result<ProfileUser>> =
        liveData {
            emit(Result.Loading)
            try {
                val authHeader = "Bearer $token"
                val profileUser = ProfileUser(
                    profile.age ?: 0,
                    profile.weight ?: 0,
                    profile.height ?: 0,
                    profile.gender ?: ""
                )
                val response = apiService.putProfile(authHeader, profileUser)
                emit(Result.Success(response))
                Log.d(TAG, "putProfile: $response")
            } catch (e: Exception) {
                emit(Result.Error(e.message.toString()))
                Log.d(TAG, "putProfile: ${e.message.toString()}")
            }
        }

    fun putPhoto(token: String, photo: Photo): LiveData<Result<Photo>> =
        liveData {
            emit(Result.Loading)
            try {
                val authHeader = "Bearer $token"
                val response = apiService.putPhoto(authHeader, photo)
                emit(Result.Success(response))
                Log.d(TAG, "putPhoto: $response")
            } catch (e: Exception) {
                emit(Result.Error(e.message.toString()))
                Log.d(TAG, "putPhoto: ${e.message.toString()}")
            }
        }

    fun recognizeFood(
        token: String,
        file: MultipartBody.Part
    ): LiveData<Result<List<ResultsItem?>?>> =
        liveData {
            emit(Result.Loading)
            try {
                val authHeader = "Bearer $token"
                val response = apiService.postRecognize(authHeader, file)
                emit(Result.Success(response.results))
                Log.d(TAG, "postRecognize: $response")
            } catch (e: Exception) {
                emit(Result.Error(e.message.toString()))
                Log.d(TAG, "postRecognize: ${e.message.toString()}")
            }
        }


    companion object {
        const val TAG = "Remote Repository"

        @Volatile
        private var INSTANCE: RemoteRepository? = null
        fun getInstance(
            apiService: ApiService
        ): RemoteRepository =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: RemoteRepository(apiService)
            }.also { INSTANCE = it }
    }
}