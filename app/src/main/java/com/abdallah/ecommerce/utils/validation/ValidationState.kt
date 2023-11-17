package com.abdallah.ecommerce.utils.validation

sealed class ValidationState (val msg : String = ""){
    object Valid : ValidationState()
    class Invalid( msg : String) : ValidationState(msg)
}

class ValidationEmailAndPass(
    val email : ValidationState,
    val password : ValidationState
)
class RegisterValidation(
    val email : ValidationState,
    val password : ValidationState,
    val firstName : ValidationState,
    val lastName : ValidationState,
)