package com.abdallah.ecommerce.data.model

class CartProduct(
    val product: Product,
    val quantity: Int,
    val color: Int,
    val size: String
){
    constructor() : this(Product() , 1 , 1 , "")
}
