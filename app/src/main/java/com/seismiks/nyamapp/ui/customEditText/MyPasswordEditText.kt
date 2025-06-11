package com.seismiks.nyamapp.ui.customEditText

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import com.seismiks.nyamapp.R
import java.util.regex.Pattern

class MyPasswordEditText @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : AppCompatEditText(context, attrs) {

    private val specialCharPattern = Pattern.compile("[^a-zA-Z0-9]")
    private val uppercaseCharPattern = Pattern.compile("[A-Z]")

    init {
        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val password = text.toString()
                var errorMessage: String? = null
                if (password.length < 8) {
                    errorMessage = context.getString(R.string.password_minimal_8_karakter)
                } else if (!specialCharPattern.matcher(password).find()) {
                    errorMessage = "Password harus memiliki minimal satu karakter spesial."
                } else if (!uppercaseCharPattern.matcher(password).find()) {
                    errorMessage = "Password harus memiliki minimal satu huruf kapital."
                }

                if (errorMessage != null) {
                    setError(errorMessage, null)
                } else {
                    // Hapus error jika semua kondisi terpenuhi
                    setError(null, null)
                }
            }

            override fun afterTextChanged(s: Editable?) { //not needed

            }

        })
    }
}