package com.abdallah.ecommerce.data.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class RatingModel (
    var raterId : String ="",
    var raterName : String ="",
    var rating : Float = 0f,
    var comment : String =""
): Parcelable