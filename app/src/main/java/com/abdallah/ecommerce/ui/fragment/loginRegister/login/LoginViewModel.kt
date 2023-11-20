package com.abdallah.ecommerce.ui.fragment.loginRegister.login

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.abdallah.ecommerce.utils.InternetConnection
import com.abdallah.ecommerce.utils.Resource
import com.abdallah.ecommerce.utils.validation.ValidationEmailAndPass
import com.abdallah.ecommerce.utils.validation.ValidationState
import com.abdallah.ecommerce.utils.validation.validateEmail
import com.abdallah.ecommerce.utils.validation.validatePassword
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    application: Application,

    ) : AndroidViewModel(application) {

    private val _noInternet = Channel<Boolean>()
    val noInternet = _noInternet.receiveAsFlow()

    private val _loginResult = Channel<Resource<AuthResult>>()
    val loginResult = _loginResult.receiveAsFlow()

    private val _failedValidation = Channel<ValidationEmailAndPass>()
    val failedValidation = _failedValidation.receiveAsFlow()

    private val _resetPassword = Channel<Resource<String>>()
    val resetPassword = _resetPassword.receiveAsFlow()

    @RequiresApi(Build.VERSION_CODES.M)
    fun login(email: String, password: String) {
        viewModelScope.launch(Dispatchers.IO) {

            if (!InternetConnection().hasInternetConnection(getApplication())) {
                _noInternet.send(true)
                return@launch

            }

            if (validData(email, password)) {
                _loginResult.send(Resource.Loading())
                firebaseAuth.signInWithEmailAndPassword(
                    email, password
                ).addOnSuccessListener { authResult ->
                    runBlocking {
                        _loginResult.send(Resource.Success(authResult))
                    }

                }.addOnFailureListener {
                    runBlocking {
                        _loginResult.send(Resource.Failure(it.message.toString()))
                    }
                }
            } else {
                val loginFailure =
                    ValidationEmailAndPass(validateEmail(email), validatePassword(password))
                _failedValidation.send(loginFailure)
            }
        }
    }

    private fun validData(email: String, password: String): Boolean {
        val emailState = validateEmail(email)
        val passwordState = validatePassword(password)

        return emailState is ValidationState.Valid && passwordState is ValidationState.Valid
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun resetPassword(email: String) {
        viewModelScope.launch(Dispatchers.IO) {

            if (!InternetConnection().hasInternetConnection(getApplication())) {
                _noInternet.send(true)
                return@launch
            }
        firebaseAuth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                runBlocking {
                    _resetPassword.send(Resource.Success("Email send successfully"))
                }
            }
            .addOnFailureListener{
                runBlocking {
                    _resetPassword.send(Resource.Failure(it.message))
                }
            }
        }


    }
}