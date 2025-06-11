package com.seismiks.nyamapp.data.remote.response

import com.google.gson.annotations.SerializedName

data class ChatResponse(
    @field:SerializedName("response")
    val response: String? = null
)