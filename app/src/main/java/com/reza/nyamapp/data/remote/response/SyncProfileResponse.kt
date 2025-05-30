package com.reza.nyamapp.data.remote.response

import com.google.gson.annotations.SerializedName

data class SyncProfileResponse(

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("user")
	val user: User? = null
)

data class User(

	@field:SerializedName("photoUrl")
	val photoUrl: String? = null,

	@field:SerializedName("displayName")
	val displayName: String? = null,

	@field:SerializedName("profile")
	val profile: Profile? = null,

	@field:SerializedName("email")
	val email: String? = null
)
