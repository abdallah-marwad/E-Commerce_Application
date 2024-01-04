package com.abdallah.ecommerce.data.model

data class UserData (
    var email : String ="",
    var favorites : ArrayList<String>? = null ,
    var cartProducts : ArrayList<String>? = null ,
)