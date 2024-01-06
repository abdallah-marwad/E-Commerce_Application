package com.abdallah.ecommerce.ui.activity

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.abdallah.ecommerce.R
import com.abdallah.ecommerce.utils.LangHelper
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginRegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("test" , "screen : LoginRegisterActivity")
        LangHelper.makeLangEn()
        setContentView(R.layout.activity_login_register)
    }
}