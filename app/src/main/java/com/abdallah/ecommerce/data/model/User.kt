package com.abdallah.ecommerce.data.model

data class User(
    val firstName : String,
    val lastName : String,
    val email : String,
    val imgPath : String,
){
    constructor() : this("","","","")
}
