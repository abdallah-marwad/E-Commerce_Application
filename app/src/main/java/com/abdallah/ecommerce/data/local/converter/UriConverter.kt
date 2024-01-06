package com.abdallah.ecommerce.data.local.converter

import android.net.Uri
import androidx.room.TypeConverter
import com.google.common.reflect.TypeToken


class UriConverter {
        @TypeConverter
        fun fromString(value: String): Uri {
            return Uri.parse(value)
        }

        @TypeConverter
        fun fromUri(uri: Uri): String {
            return uri.toString()
        }
    }