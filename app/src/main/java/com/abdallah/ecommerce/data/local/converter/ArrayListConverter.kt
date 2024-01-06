package com.abdallah.ecommerce.data.local.converter

import androidx.room.TypeConverter
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import java.lang.reflect.Type


class ArrayListConverter {
    @TypeConverter
    fun stringListToJson(value: ArrayList<String>) = Gson().toJson(value)
    @TypeConverter
    fun intListToJson(value: ArrayList<Int>) = Gson().toJson(value)

    @TypeConverter
    fun jsonStringToList(value: String?): ArrayList<String> {
        val listType: Type = object : TypeToken<ArrayList<String>>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun jsonIntToList(value: String?): ArrayList<Int> {
        val listType: Type = object : TypeToken<ArrayList<Int>>() {}.type
        return Gson().fromJson(value, listType)
    }
}

