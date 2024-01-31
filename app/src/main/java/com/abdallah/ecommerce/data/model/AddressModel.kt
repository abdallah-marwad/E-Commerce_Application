package com.abdallah.ecommerce.data.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.io.Serializable

@Parcelize
data class AddressModel(
    val id: String,
    val addressTitle: String,
    val fullName: String,
    val street: String,
    val phone: String,
    val city: String,
    val state: String,
    var isSelected: Boolean = false
) : Parcelable, Serializable {
    constructor() : this("", "", "", "", "", "", "")
}
