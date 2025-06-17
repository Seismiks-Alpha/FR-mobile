package com.seismiks.nyamapp.utils

import com.seismiks.nyamapp.data.remote.response.FoodHistory
import com.seismiks.nyamapp.data.remote.response.HistoryViewItem
import com.seismiks.nyamapp.data.remote.response.RecordsItem

object HistoryUtil {
    fun FoodHistory.toHistoryViewItem(): HistoryViewItem {
        return HistoryViewItem(
            id = this.id ?: "", // Ambil ID dari FoodHistory
            foodName = this.food.foodType ?: "Nama tidak tersedia",
            calories = this.food.calories ?: 0,
            date = this.date ?: "",
            imageUrl = this.imageUrl,
            grams = this.grams ?: 0.0,
            protein = this.food.protein ?: 0.0,
            fat = this.food.fat ?: 0.0,
            carbohydrates = this.food.carbohydrates ?: 0.0
        )
    }

    // Mapper dari HistoryAllResponse -> RecordsItem
    fun RecordsItem.toHistoryViewItem(): HistoryViewItem {
        return HistoryViewItem(
            id = this.id ?: "",
            foodName = this.food?.name ?: "Nama tidak tersedia",
            calories = this.food?.calories ?: 0,
            date = this.date ?: "",
            imageUrl = this.imageUrl,
            grams = this.grams ?: 0.0,
            protein = this.food?.protein ?: 0.0,
            fat = this.food?.fat ?: 0.0,
            carbohydrates = this.food?.carbohydrates ?: 0.0
        )
    }
}