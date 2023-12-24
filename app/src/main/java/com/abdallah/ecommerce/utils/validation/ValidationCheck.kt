package com.abdallah.ecommerce.utils.validation

import android.util.Patterns
import android.widget.EditText

private const val MIN_PASSWORD_LENGTH = 6
fun validateEmail(email : String) : ValidationState {
    return when {
        email.isEmpty() -> ValidationState.Invalid("Email cannot be empty")

        !Patterns.EMAIL_ADDRESS.matcher(email).matches() ->
            ValidationState.Invalid("Please enter a valid email format")

        else -> ValidationState.Valid
    }
}

fun validatePassword(password : String) : ValidationState {
    if(password.isEmpty())
        return ValidationState.Invalid("Password cannot be empty")
    if (password.length < MIN_PASSWORD_LENGTH)
        return ValidationState.Invalid("Password should contain more than ass6 chars")

    return ValidationState.Valid
}

fun validateInputAsNotEmpty(email: String): ValidationState {
    if (email.isEmpty())
        return ValidationState.Invalid("This filed is required")

    return ValidationState.Valid
}

fun validateInputAsNotEmpty(ed: EditText, errTxt: String): Boolean {
    if (ed.text.toString().isEmpty()) {
        ed.error = errTxt
        return false
    }


    return true
}

