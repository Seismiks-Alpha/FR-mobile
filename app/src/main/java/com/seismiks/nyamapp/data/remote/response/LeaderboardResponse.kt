package com.seismiks.nyamapp.data.remote.response

import com.google.gson.annotations.SerializedName

data class LeaderboardResponse (
    @field:SerializedName("top5")
    val top5: List<LeaderboardData>,

    @field:SerializedName("currentUser")
    val currentUser: CurrentUser
)

data class LeaderboardData (
    @field:SerializedName("rank")
    val rank: Int,

    @field:SerializedName("fullname")
    val fullName: String,

    @field:SerializedName("photoUrl")
    val photoUrl: String,

    @field:SerializedName("points")
    val points: Int,
)

data class CurrentUser (
    @field:SerializedName("fullname")
    val fullName: String,

    @field:SerializedName("rank")
    val rank: Int,

    @field:SerializedName("points")
    val points: Int,
)