package com.abdallah.ecommerce.ui.fragment.loginRegister.registeration

import android.app.Activity
import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.abdallah.ecommerce.data.firebase.FirebaseManager
import com.abdallah.ecommerce.utils.InternetConnection
import com.abdallah.ecommerce.utils.Resource
import com.abdallah.ecommerce.utils.validation.RegisterValidation
import com.abdallah.ecommerce.utils.validation.ValidationState
import com.abdallah.ecommerce.utils.validation.validateEmail
import com.abdallah.ecommerce.utils.validation.isInputNotEmpty
import com.abdallah.ecommerce.utils.validation.validatePassword
import com.google.firebase.auth.FirebaseAuth
import com.abdallah.ecommerce.data.model.User
import com.abdallah.ecommerce.data.firebase.registeration.RegisterWithGoogle
import com.abdallah.ecommerce.data.firebase.registeration.RegisterWithPhone
import com.abdallah.ecommerce.data.model.UserData
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
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
    private val google: RegisterWithGoogle,
    private val phoneRegister: RegisterWithPhone,
    private val fireStore: FirebaseFirestore,
    application: Application,

    ) : AndroidViewModel(application) {



    private val _noInternet = Channel<Boolean>()
    val noInternet = _noInternet.receiveAsFlow()

    private val _validationState = Channel<RegisterValidation>()
    val validationState = _validationState.receiveAsFlow()

    private val _register = MutableStateFlow<Resource<String>>(Resource.UnSpecified())
    val register: Flow<Resource<String>> = _register

    private val _saveUserData = MutableStateFlow<Resource<String>>(Resource.UnSpecified())
    val saveUserData: Flow<Resource<String>> = _saveUserData

    val googleRegister: Flow<Resource<String>> = google.googleRegister

    val phoneRegisterState = phoneRegister.phoneRegisterState
    val sendOtpState = phoneRegister.sendOtpState


    fun sendVerificationCode(phoneNumber : String , activity: Activity){
        viewModelScope.launch (Dispatchers.IO){
            phoneRegister.sendVerificationCode(phoneNumber,activity)
        }
    }
    fun registerWithPhone(otp : String){
        viewModelScope.launch (Dispatchers.IO){
            phoneRegister.registerWithPhone(otp)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun createAccountWithEmailAndPassword(
        user: User,
        password: String,
    ) {
        viewModelScope.launch {
            if (!InternetConnection().hasInternetConnection(getApplication())) {
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
            isInputNotEmpty(user.firstName), isInputNotEmpty(user.lastName)
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
     @RequiresApi(Build.VERSION_CODES.M)
     suspend fun googleSignInRequest(activity : Activity) : GoogleSignInClient? {
         if(!InternetConnection().hasInternetConnection(getApplication())){
                 _noInternet.send(true)
                 return null

         }
         return google.googleSignInRequest(activity)
     }




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
        val firstNameState = isInputNotEmpty(firstName)
        val lastNameState = isInputNotEmpty(lastName)

        return emailState is ValidationState.Valid &&
                passwordState is ValidationState.Valid &&
                firstNameState is ValidationState.Valid &&
                lastNameState is ValidationState.Valid
    }
}