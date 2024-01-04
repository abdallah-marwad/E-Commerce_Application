package com.abdallah.ecommerce.data.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize
import org.jetbrains.annotations.NotNull
@Parcelize
//@Entity(tableName = "product_categories")

data class Category(
//    @PrimaryKey(autoGenerate = true)
    var id : Int = 0,
    var categoryName : String ? = null,
    var image:String? = null,
    var isSelected : Boolean = false
): Parcelable

@Parcelize
 class Categories(): ArrayList<Category>(), Parcelable
