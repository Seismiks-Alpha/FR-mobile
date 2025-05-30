package com.reza.nyamapp.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

const val formatDate = "dd-MM-yyyy"
val sdf = SimpleDateFormat(formatDate, Locale.US)
var calendar: Calendar = Calendar.getInstance()

fun Long.convertLongToTime(): String {
    calendar.timeInMillis = this
    return sdf.format(calendar.timeInMillis)
}