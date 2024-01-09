package com.abdallah.ecommerce.data.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

//@Entity(tableName = "products")
@Parcelize
data class Product (
//    @PrimaryKey
    var id : String = "",
    var productName: String? = null,
    var categoryName: String? = null,
    var productdescription: String? = null,
    var productQuantity: Int? = null,
    var price: Double? = null,
    var offerValue: Double? = null,
    var hasOffer: Boolean = false,
    var offerPercentage: Double? = null,
    var date: Long? = null,
    var productImages: ArrayList<String>? = null,
    var productSize: ArrayList<String>? = null,
    var productColors: ArrayList<Int>? = null,
    var ratingList: ArrayList<RatingModel> = ArrayList(),
    var ratersNum: Int? = 0,
    var rating: Float = 5f,
    var productMainImg: String? = null,
    var location: String? = null,

    ):Parcelable