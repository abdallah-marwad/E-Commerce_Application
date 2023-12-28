package com.abdallah.ecommerce.utils.validation

import android.util.Patterns
import android.widget.EditText
import com.abdallah.ecommerce.utils.Constant

private const val MIN_PASSWORD_LENGTH = 6
fun validateEmail(email : String) : ValidationState {
    return when {
        email.isEmpty() -> ValidationState.Invalid( Constant.REQUIRED_FIELD)

        !Patterns.EMAIL_ADDRESS.matcher(email).matches() ->
            ValidationState.Invalid(Constant.PLS_ENTER_VALID_EMAIL)

        else -> ValidationState.Valid
    }
}

fun validatePassword(password : String) : ValidationState {
    if(password.isEmpty())
        return ValidationState.Invalid( Constant.REQUIRED_FIELD)
    if (password.length < MIN_PASSWORD_LENGTH)
        return ValidationState.Invalid("Password should contain more than 6 chars")

    return ValidationState.Valid
}

fun isInputNotEmpty(email: String): ValidationState {
    if (email.isEmpty())
        return ValidationState.Invalid( Constant.REQUIRED_FIELD)

    return ValidationState.Valid
}

fun isInputNotEmpty(ed: EditText, errTxt: String): Boolean {
    if (ed.text.toString().isEmpty()) {
        ed.error = errTxt
        return false
    }

    return true
}
fun isPhoneNumberValid(ed: EditText): Boolean {
    val phoneNumber= ed.text.toString()
    if (phoneNumber.isEmpty()){
        ed.error = Constant.REQUIRED_FIELD
        return false
    }
     if (phoneNumber.length !=11){
        ed.error = Constant.PLS_ENTER_VALID_PHONE
        return false
    }
    if (phoneNumber.startsWith("015").not() &&
        phoneNumber.startsWith("010").not()&&
        phoneNumber.startsWith("012").not()&&
        phoneNumber.startsWith("011").not()
        ){
        ed.error = Constant.PLS_ENTER_VALID_PHONE
        return false
    }



    return true
}

