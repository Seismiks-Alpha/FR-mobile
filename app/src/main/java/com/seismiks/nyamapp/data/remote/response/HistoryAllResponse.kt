package com.seismiks.nyamapp.data.remote.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

data class HistoryAllResponse(

	@field:SerializedName("records")
	val records: List<RecordsItem?>? = null,

	@field:SerializedName("totalCalories")
	val totalCalories: Int? = null,

	@field:SerializedName("userId")
	val userId: String? = null
)

@Parcelize
data class FoodAll(

	@field:SerializedName("carbohydrates")
	val carbohydrates: Double? = null,

	@field:SerializedName("protein")
	val protein: Double? = null,

	@field:SerializedName("foodType")
	val name: String? = null,

	@field:SerializedName("fat")
	val fat: Double? = null,

	@field:SerializedName("calories")
	val calories: Int? = null,
) : Parcelable

@Parcelize
data class RecordsItem(

	@field:SerializedName("id")
	val id: String? = null,

	@field:SerializedName("userId")
	val userId: String? = null,

	@field:SerializedName("date")
	val date: String? = null,

	@field:SerializedName("grams")
	val grams: Double? = null,

	@field:SerializedName("imageUrl")
	val imageUrl: String? = null,

	@field:SerializedName("food")
	val food: FoodAll? = null
) : Parcelable
