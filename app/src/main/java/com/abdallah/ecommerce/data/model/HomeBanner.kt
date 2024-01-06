package com.abdallah.ecommerce.data.model

import android.net.Uri
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.abdallah.ecommerce.data.local.converter.UriConverter

//@Entity(tableName = "home_banner")

data class HomeBanner (
//    @PrimaryKey(autoGenerate = true)
    var id : Int = 0,
//    @TypeConverters(UriConverter::class)
    var image:Uri? = null,
)