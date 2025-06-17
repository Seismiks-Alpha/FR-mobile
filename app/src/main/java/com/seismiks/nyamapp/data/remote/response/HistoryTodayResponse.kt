package com.seismiks.nyamapp.data.remote.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

data class HistoryTodayResponse(
    @field:SerializedName("today")
    val today: InfoToday,

    @field:SerializedName("yesterday")
    val yesterday: InfoYesterday,

    @field:SerializedName("progress")
    val progress: Progress
)

data class InfoToday(
    @field:SerializedName("totalCalories")
    val totalCalories: Int,

    @field:SerializedName("foodHistory")
    val foodHistory: List<FoodHistory>
)

data class InfoYesterday(
    @field:SerializedName("totalCalories")
    val totalCalories: Int,

    @field:SerializedName("foodHistory")
    val foodHistory: List<FoodHistory>
)

data class Progress (
    @field:SerializedName("recommended")
    val recommended: Int,

    @field:SerializedName("custom")
    val current: Int,

    @field:SerializedName("percenOfRecommended")
    val percenOfRecommended: Int,

    @field:SerializedName("percenOfCustom")
    val percenOfCustom: Int
)

@Parcelize
data class FoodHistory(
    @field:SerializedName("id")
    val id: String,

    @field:SerializedName("userId")
    val userId: String,

    @field:SerializedName("date")
    val date: String,

    @field:SerializedName("grams")
    val grams: Double,

    @field:SerializedName("imageUrl")
    val imageUrl: String,

    @field:SerializedName("food")
    val food: FoodToday
) : Parcelable

@Parcelize
data class FoodToday(
    @field:SerializedName("foodType")
    val foodType: String,

    @field:SerializedName("carbohydrates")
    val carbohydrates: Double,

    @field:SerializedName("protein")
    val protein: Double,

    @field:SerializedName("fat")
    val fat: Double,

    @field:SerializedName("calories")
    val calories: Int
) : Parcelable
