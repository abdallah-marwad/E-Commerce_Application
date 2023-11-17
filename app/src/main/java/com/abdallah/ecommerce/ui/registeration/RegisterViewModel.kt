package com.abdallah.ecommerce.ui.registeration

import android.app.Activity
import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abdallah.ecommerce.utils.InternetConnection
import com.abdallah.ecommerce.utils.Resource
import com.abdallah.ecommerce.utils.validation.RegisterValidation
import com.abdallah.ecommerce.utils.validation.ValidationState
import com.abdallah.ecommerce.utils.validation.validateEmail
import com.abdallah.ecommerce.utils.validation.validateInputAsNotEmpty
import com.abdallah.ecommerce.utils.validation.validatePassword
import com.google.firebase.auth.FirebaseAuth
import com.abdallah.ecommerce.data.model.User
import com.abdallah.ecommerce.data.registeration.RegisterWithGoogle
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.UserProfileChangeRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val google:  RegisterWithGoogle,

) : ViewModel() {



    private val _noInternet = Channel<Boolean>()
    val noInternet = _noInternet.receiveAsFlow()

    private val _validationState = Channel<RegisterValidation>()
    val validationState = _validationState.receiveAsFlow()

    private val _register = MutableStateFlow<Resource<String>>(Resource.UnSpecified())
    val register: Flow<Resource<String>> = _register

    @RequiresApi(Build.VERSION_CODES.M)
    fun createAccountWithEmailAndPassword(
        user: User,
        password: String,
        application: Application
    ) {
        viewModelScope.launch {
            if (!InternetConnection().hasInternetConnection(application)) {
                _noInternet.send(true)
                return@launch
            }
            if (!registerValidation(user.email, password, user.firstName, user.lastName)) {
                emitValidationErr(user, password)
                return@launch
            }

            registerWithEmailAndPassword(user, password)
        }
    }

    private fun emitValidationErr(user: User, password: String) {
        val registerFailed = RegisterValidation(
            validateEmail(user.email), validatePassword(password),
            validateInputAsNotEmpty(user.firstName), validateInputAsNotEmpty(user.lastName)
        )
        runBlocking {
            _validationState.send(registerFailed)
        }
    }


    private fun registerWithEmailAndPassword(user: User, password: String) {

        runBlocking { _register.emit(Resource.Loading()) }

        firebaseAuth.createUserWithEmailAndPassword(user.email, password)
            .addOnSuccessListener { authResult ->

                authResult.user?.let {
                    addNameToUserProfile(concatenateUserName(user.firstName, user.lastName))
                }
            }
            .addOnFailureListener { e ->
                runBlocking {
                    _register.emit(Resource.Failure(e.message.toString()))
                }
            }
    }
     fun googleSignInRequest(activity : Activity) =
         google.googleSignInRequest(activity)



     fun googleAuthWithFireBase(account: GoogleSignInAccount) {
         viewModelScope.launch {
             google.googleAuthWithFireBase(account)

         }

    }

    private fun addNameToUserProfile(
        userName: String,
    ) {
        val user = firebaseAuth.currentUser
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(userName)
            .build()



        user?.let {
            it.updateProfile(profileUpdates)
                .addOnSuccessListener {
                    runBlocking {
                        _register.emit(Resource.Success(userName))
                    }
                }.addOnFailureListener {
                    runBlocking {
                        _register.emit(Resource.Failure(it.message.toString()))
                    }
                }
        }
    }

    private fun concatenateUserName(firstName: String, lastName: String): String {
        return (firstName.trim() + lastName.trim()).capitalize()
    }

    private fun registerValidation(
        email: String,
        password: String,
        firstName: String,
        lastName: String
    ): Boolean {
        val emailState = validateEmail(email)
        val passwordState = validatePassword(password)
        val firstNameState = validateInputAsNotEmpty(firstName)
        val lastNameState = validateInputAsNotEmpty(lastName)

        return emailState is ValidationState.Valid &&
                passwordState is ValidationState.Valid &&
                firstNameState is ValidationState.Valid &&
                lastNameState is ValidationState.Valid
    }
}