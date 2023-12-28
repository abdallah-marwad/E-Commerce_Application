package com.abdallah.ecommerce.data.model


data class Product (
    var id : String? = null,
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
    var productMainImg: String? = null,
    var location: String? = null,

)