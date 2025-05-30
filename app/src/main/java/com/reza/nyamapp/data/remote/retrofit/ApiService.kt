package com.reza.nyamapp.data.remote.retrofit

import com.reza.nyamapp.data.remote.response.ChatResponse
import com.reza.nyamapp.data.remote.response.ProfileResponse
import com.reza.nyamapp.data.remote.response.SyncProfileResponse
import com.reza.nyamapp.data.remote.retrofit.chat.PostChat
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT

interface ApiService {
    @POST("api/auth/sync")
    suspend fun syncProfile(
        @Header("Authorization") authorizationHeader: String,
    ): SyncProfileResponse

    @POST("chat")
    suspend fun postQuestion(
        @Header("Authorization") Bearer: String,
        @Body postChat: PostChat
    ): ChatResponse

    @PUT("api/profile")
    suspend fun putProfile(
        @Header("Authorization") Bearer: String,
        @Body profileUser: ProfileUser
    ): ProfileUser

    @GET("api/profile")
    suspend fun getProfile(
        @Header("Authorization") Bearer: String
    ): ProfileResponse

    @PUT("api/photo")
    suspend fun putPhoto(
        @Header("Authorization") Bearer: String,
        @Body photo: Photo
    ): Photo

}