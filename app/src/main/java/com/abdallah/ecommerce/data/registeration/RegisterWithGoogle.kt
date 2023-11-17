package com.abdallah.ecommerce.data.registeration

import android.app.Activity
import android.util.Log
import com.abdallah.ecommerce.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import javax.inject.Inject

class RegisterWithGoogle @Inject constructor(private var auth: FirebaseAuth) {


    fun googleAuthWithFireBase(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential)
            .addOnSuccessListener {
                Log.d("tests", "addOnSuccessListener google")
            }.addOnFailureListener {
                Log.d("tests", "addOnFailureListener google")

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