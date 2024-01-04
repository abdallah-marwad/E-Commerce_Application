package com.abdallah.ecommerce.data.sharedPreferences

import android.content.Context
import android.content.SharedPreferences
import com.abdallah.ecommerce.application.MyApplication

object SharedPreferencesHelper {
    private val preferences : SharedPreferences = MyApplication.myAppContext.getSharedPreferences("ecommerce" , Context.MODE_PRIVATE)

    fun addBoolean(key : String , value : Boolean){
        preferences.edit().putBoolean(key, value).apply()
    }
    fun addString(key : String , value : String){
        preferences.edit().putString(key, value).apply()
    }

    fun getBoolean (key : String) : Boolean{
     return preferences.getBoolean(key , false)
    }
    fun getString (key : String) : String{
     return preferences.getString(key , "") ?: ""
    }
}