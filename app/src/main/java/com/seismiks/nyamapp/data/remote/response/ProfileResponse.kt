package com.seismiks.nyamapp.data.remote.response

import com.google.gson.annotations.SerializedName

data class ProfileResponse(

    @field:SerializedName("email")
    val email: String? = null,

    @field:SerializedName("displayName")
    val displayName: String? = null,

    @field:SerializedName("photoUrl")
    val photoUrl: String? = null,

    @field:SerializedName("profile")
    val profile: Profile? = null
)

data class Profile(

    @field:SerializedName("gender")
    val gender: String? = null,

    @field:SerializedName("weight")
    val weight: Int? = null,

    @field:SerializedName("age")
    val age: Int? = null,

    @field:SerializedName("height")
    val height: Int? = null
)
