package com.abdallah.ecommerce.ui.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import com.abdallah.ecommerce.R
import com.abdallah.ecommerce.data.sharedPreferences.SharedPreferencesHelper
import com.abdallah.ecommerce.utils.Constant
import com.abdallah.ecommerce.utils.LangHelper
import com.abdallah.ecommerce.utils.LangHelper.makeLangEn
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.util.Locale

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        Log.d("test" , "screen : SplashActivity")
        makeLangEn()
        setContentView(R.layout.activity_splash)
        runBlocking {
            delay(2000)
        }


        if(SharedPreferencesHelper.getBoolean(Constant.IS_LOGGED_IN) or
            SharedPreferencesHelper.getBoolean(Constant.IS_SKIP)) {
            startActivity(Intent(this , ShoppingActivity::class.java))
            finish()
            return
        }

        startActivity(Intent(this , LoginRegisterActivity::class.java))
        finish()


    }


}