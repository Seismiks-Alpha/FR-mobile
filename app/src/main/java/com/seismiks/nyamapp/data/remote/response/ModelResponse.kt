package com.seismiks.nyamapp.data.remote.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

data class ModelResponse(
    @field:SerializedName("message")
    val message: String,

    @field:SerializedName("data")
    val data: List<MealData>
)

@Parcelize
data class MealData(
    @field:SerializedName("id")
    val id: String,

    @field:SerializedName("userId")
    val userId: String,

    @field:SerializedName("date")
    val date: String,

    @field:SerializedName("grams")
    val grams: Double,

    @field:SerializedName("foodType")
    val foodType: String,

    @field:SerializedName("carbohydrates")
    val carbohydrates: Double,

    @field:SerializedName("protein")
    val protein: Double,

    @field:SerializedName("fat")
    val fat: Double,

    @field:SerializedName("calories")
    val calories: Double,

    @field:SerializedName("imageUrl")
    val imageUrl: String
) : Parcelable
