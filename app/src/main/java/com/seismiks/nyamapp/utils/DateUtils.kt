package com.seismiks.nyamapp.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object DateUtils {
    fun formatDate(dateString: String, locale: Locale = Locale.getDefault()): String {
        val inputFormat = SimpleDateFormat("dd/MM/yyyy", locale)

        val date = inputFormat.parse(dateString)

        val outputFormat: SimpleDateFormat = when (locale.language) {
            "in" -> SimpleDateFormat("EEEE, d MMMM yyyy", locale)
            else -> SimpleDateFormat("EEEE, MMMM d'th' yyyy", locale)
        }

        return outputFormat.format(date!!)
    }

    fun formatTanggalWIB_Legacy(isoString: String, pattern: String): String {
        try {
            // 1. Buat formatter untuk MEMBACA string input (format ISO 8601 UTC)
            // Pola 'Z' di sini akan menangani 'Z' (Zulu/UTC) pada string input
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")

            // 2. Parse string input menjadi objek Date
            val date = inputFormat.parse(isoString) ?: return "Tanggal tidak valid"

            // 3. Buat formatter untuk MENULIS string output (format yang diinginkan di WIB)
            val outputFormat = SimpleDateFormat(pattern, Locale("id", "ID"))
            outputFormat.timeZone = TimeZone.getTimeZone("Asia/Jakarta")

            // 4. Format objek Date menjadi string output
            return outputFormat.format(date)
        } catch (e: Exception) {
            e.printStackTrace()
            return "Format tanggal tidak valid"
        }
    }

    fun String.toFormattedDateTime(): String {
        // Menggunakan kembali fungsi yang sudah ada dan terbukti aman.
        // Anda bisa mengubah pola format sesuai kebutuhan.
        val pattern = "dd MMMM yyyy, HH.mm z"
        return formatTanggalWIB_Legacy(this, pattern)
    }
}