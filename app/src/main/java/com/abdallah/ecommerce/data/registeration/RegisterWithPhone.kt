package com.abdallah.ecommerce.data.registeration

import android.app.Activity
import android.util.Log
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider


class RegisterWithPhone (


){
    lateinit var storedVerificationId: String
    lateinit var activity: Activity
    lateinit var auth: FirebaseAuth
    private fun verifyOtp(otp: String) {
        val credential = PhoneAuthProvider.getCredential(storedVerificationId , otp)
        signInWithPhoneAuthCredential(credential)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth = FirebaseAuth.getInstance()
        auth.signInWithCredential(credential)
            .addOnCompleteListener {
                if(it.isSuccessful){
                    Log.d("tests" , "Phone registration isSuccessful")
                }
            }.addOnFailureListener {
                Log.d("tests" , "registration Failure")

            }
    }

     fun sendOtp(number : String) {

        if (number == "") {
            return
        }
         sendVerificationCode(number)

     }

    private fun sendVerificationCode(number: String) {
        auth = FirebaseAuth.getInstance()
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber("+20$number")
            .setTimeout(60L, java.util.concurrent.TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private val callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks =
        object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                Log.d("test", "onVerificationCompleted")

                verifyOtp(credential.smsCode ?:"")

            }

            override fun onVerificationFailed(e: FirebaseException) {
                Log.d("test", "FirebaseException ${e.localizedMessage}")

                if (e is FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                } else if (e is FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                }

                // Show a message and update the UI
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken,
            ) {
                Log.d("test", "onCodeSent with $verificationId")


                storedVerificationId = verificationId
            }
        }


}