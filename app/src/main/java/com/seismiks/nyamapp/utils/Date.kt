package com.seismiks.nyamapp.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Date {
    fun formatDate(dateString: String, locale: Locale = Locale.getDefault()): String {
        val inputFormat = SimpleDateFormat("dd/MM/yyyy", locale)

        val date = inputFormat.parse(dateString)

        val outputFormat: SimpleDateFormat = when (locale.language) {
            "in" -> SimpleDateFormat("EEEE, d MMMM yyyy", locale)
            else -> SimpleDateFormat("EEEE, MMMM d'th' yyyy", locale)
        }

        return outputFormat.format(date!!)
    }

    fun Long.convertLongToTime(): String {
        val date = Date(this)
        val format = SimpleDateFormat("HH:mm", Locale.getDefault())
        return format.format(date)
    }
}