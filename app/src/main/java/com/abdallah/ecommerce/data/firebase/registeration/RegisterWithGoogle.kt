package com.abdallah.ecommerce.data.firebase.registeration

import android.app.Activity
import android.util.Log
import com.abdallah.ecommerce.R
import com.abdallah.ecommerce.utils.Resource
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

class RegisterWithGoogle @Inject constructor(private var auth: FirebaseAuth) {


    private val _googleRegister = MutableStateFlow<Resource<String>>(Resource.UnSpecified())
    val googleRegister: Flow<Resource<String>> = _googleRegister

    suspend fun googleAuthWithFireBase(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)

        _googleRegister.emit(Resource.Loading())
        auth.signInWithCredential(credential)
            .addOnSuccessListener {
                runBlocking(){
                    _googleRegister.value = (Resource.Success(""))
                }


            }.addOnFailureListener {
                runBlocking(){
                    _googleRegister.value = (Resource.Failure(it.message))
                }
            }


    }

    fun googleSignInRequest(activity: Activity): GoogleSignInClient {
        val option = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(activity.getString(R.string.webClint_id))
            .requestEmail()
            .requestProfile()
            .build()

        return GoogleSignIn.getClient(activity, option)


    }
}