package com.seismiks.nyamapp.utils

import android.os.Build
import android.text.Html
import android.text.Spanned

fun formatTextWithBold(text: String): Spanned {
    val boldRegex = Regex("\\*\\*(.*?)\\*\\*")

    val htmlText = text
        .replace(boldRegex, "<b>$1</b>")
        .replace("\n", "<br>")

    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        Html.fromHtml(htmlText, Html.FROM_HTML_MODE_LEGACY)
    } else {
        @Suppress("DEPRECATION")
        Html.fromHtml(htmlText)
    }
}