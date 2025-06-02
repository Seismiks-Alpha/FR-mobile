package com.reza.nyamapp.data.remote.response

import com.google.gson.annotations.SerializedName

data class RecognizeResponse(

	@field:SerializedName("results")
	val results: List<ResultsItem?>? = null
)

data class ResultsItem(

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("grams")
	val grams: Int? = null
)
