package com.abdallah.ecommerce.utils

import android.app.Activity
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import androidx.core.app.ActivityCompat.recreate
import com.abdallah.ecommerce.application.MyApplication
import java.util.Locale


object LangHelper {
        private fun setLocale(lang: String) {
            val locale = Locale(lang)
            Locale.setDefault(locale)
            val resources: Resources = MyApplication.myAppContext.resources
            var configuration: Configuration = resources.configuration
            configuration.setLocale(locale)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                configuration = MyApplication.myAppContext.createConfigurationContext(configuration).resources.configuration;
            }

            resources.updateConfiguration(configuration, resources.displayMetrics)
        }
     fun makeLangEn() {
        if(Locale.getDefault().displayLanguage !="en"){
            setLocale("en");
        }


    }

}