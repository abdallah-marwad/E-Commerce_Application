package com.abdallah.ecommerce.data.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class AddressModel(
    val addressTitle:String,
    val fullName:String,
    val street:String,
    val phone:String,
    val city:String,
    val state:String,
    var isSelected:Boolean = false
) : Parcelable {
    constructor():this("","","","","","")
}
