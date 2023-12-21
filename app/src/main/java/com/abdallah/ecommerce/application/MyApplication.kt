package com.abdallah.ecommerce.application

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApplication : Application() {

    private var currentAct: Activity? = null
    lateinit var mMyApp: MyApplication
        private set


    override fun onCreate() {
        super.onCreate();
        mMyApp = this.applicationContext as MyApplication
        actInstance()

    }

    fun getCurrentAct(): Activity? {
        return currentAct;
    }


    private fun actInstance() {
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(p0: Activity, p1: Bundle?) {
                currentAct = p0
                Log.d("screen", "onActivityCreated :: ${p0.javaClass.simpleName}")
            }

            override fun onActivityStarted(p0: Activity) {
            }

            override fun onActivityResumed(p0: Activity) {
            }

            override fun onActivityPaused(p0: Activity) {
            }

            override fun onActivityStopped(p0: Activity) {
            }

            override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) {
            }

            override fun onActivityDestroyed(p0: Activity) {
            }

        })
    }
}