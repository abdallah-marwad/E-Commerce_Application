package com.abdallah.ecommerce.data.sharedPreferences

import com.abdallah.ecommerce.data.model.UserData
import com.abdallah.ecommerce.utils.Constant
import com.google.gson.Gson

class UserDataHelper {
    fun saveUserDataInShared(userData: UserData) {
        val gson = Gson()
        val json = gson.toJson(userData)
        SharedPreferencesHelper.addString(Constant.USER_DATA, json)
    }

    fun getUserDataFromShared(): UserData? {
        val gson = Gson()
        val userJson = SharedPreferencesHelper.getString(Constant.USER_DATA)
        if (userJson.isEmpty())
            return null
        return gson.fromJson(userJson, UserData::class.java)
    }
}