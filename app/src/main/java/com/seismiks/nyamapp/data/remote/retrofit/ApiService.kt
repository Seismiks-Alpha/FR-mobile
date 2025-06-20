package com.seismiks.nyamapp.data.remote.retrofit

import androidx.paging.PagingSource
import com.seismiks.nyamapp.data.remote.response.ChatResponse
import com.seismiks.nyamapp.data.remote.response.HistoryAllResponse
import com.seismiks.nyamapp.data.remote.response.HistoryTodayResponse
import com.seismiks.nyamapp.data.remote.response.LeaderboardResponse
import com.seismiks.nyamapp.data.remote.response.ModelResponse
import com.seismiks.nyamapp.data.remote.response.ProfileResponse
import com.seismiks.nyamapp.data.remote.response.RecognizeResponse
import com.seismiks.nyamapp.data.remote.response.SyncProfileResponse
import com.seismiks.nyamapp.data.remote.retrofit.chat.PostChat
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path

interface ApiService {
    @POST("api/auth/sync")
    suspend fun syncProfile(
        @Header("Authorization") authorizationHeader: String,
    ): SyncProfileResponse

    @POST("chat/gemini")
    suspend fun postQuestionGemini(
        @Header("Authorization") Bearer: String,
        @Body postChat: PostChat
    ): ChatResponse

    @POST("chat")
    suspend fun postQuestionLocal(
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

    @Multipart
    @POST("api/upload")
    suspend fun postRecognize(
        @Header("Authorization") Bearer: String,
        @Part file: MultipartBody.Part
    ): ModelResponse

    @GET("api/leaderboard")
    suspend fun getLeaderboard(
        @Header("Authorization") Bearer: String
    ): LeaderboardResponse

    @GET("api/food-history/today")
    suspend fun getHistoryToday(
        @Header("Authorization") Bearer: String
    ): HistoryTodayResponse

    @GET("api/food-history/all")
    suspend fun getHistoryAll(
        @Header("Authorization") Bearer: String
    ): HistoryAllResponse

    @DELETE("api/food-history/{id}")
    suspend fun deleteHistory(
        @Header("Authorization") Bearer: String,
        @Path("id") id: String
    )

    @DELETE("api/all/food-history")
    suspend fun deleteAllHistory(
        @Header("Authorization") Bearer: String
    )

}