package com.seismiks.nyamapp.data.remote.response

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class HistoryViewItem(
    val id: String,
    val foodName: String,
    val calories: Int,
    val date: String,
    val imageUrl: String?,
    val grams: Double,
    val protein: Double,
    val fat: Double,
    val carbohydrates: Double
) : Parcelable