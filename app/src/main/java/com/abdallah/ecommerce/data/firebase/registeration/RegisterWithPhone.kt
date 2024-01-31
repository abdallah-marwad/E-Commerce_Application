package com.abdallah.ecommerce.data.firebase.registeration

import android.app.Activity
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.abdallah.ecommerce.utils.Constant
import com.abdallah.ecommerce.utils.Resource
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import javax.inject.Inject


class RegisterWithPhone @Inject constructor() {
    lateinit var storedVerificationId: String
    lateinit var auth: FirebaseAuth

    private val _phoneRegisterState = MutableLiveData<Resource<Boolean>>()
    val phoneRegisterState = _phoneRegisterState

    private val _sendOtpState = MutableLiveData<Resource<String>>()
    val sendOtpState = _sendOtpState

    fun registerWithPhone(otp: String) {
        _phoneRegisterState.postValue(Resource.Loading())
        val credential = PhoneAuthProvider.getCredential(storedVerificationId, otp)
        signInWithPhoneAuthCredential(credential)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth = FirebaseAuth.getInstance()
        auth.signInWithCredential(credential)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    _phoneRegisterState.postValue(Resource.Success(true))
                }
            }.addOnFailureListener {
                _phoneRegisterState.postValue(Resource.Failure(it.message))
            }
    }



    fun sendVerificationCode(number: String , activity: Activity) {
        _sendOtpState.postValue(Resource.Loading())

        auth = FirebaseAuth.getInstance()
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber("+20$number")
            .setTimeout(90L, java.util.concurrent.TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private val callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks =
        object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                _sendOtpState.postValue(Resource.Success(""))
                registerWithPhone(credential.smsCode ?: "")
                Log.d("test", "onVerificationCompleted")


            }

            override fun onVerificationFailed(e: FirebaseException) {
                _sendOtpState.postValue(Resource.Failure(Constant.GENERAL_ERR_MSG))

                Log.d("test", "FirebaseException ${e.localizedMessage}")

                if (e is FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                } else if (e is FirebaseTooManyRequestsException) {

                }
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken,
            ) {
                storedVerificationId = verificationId
                _sendOtpState.postValue(Resource.Success(storedVerificationId))
                Log.d("test", "onCodeSent with $verificationId")

            }
        }


}