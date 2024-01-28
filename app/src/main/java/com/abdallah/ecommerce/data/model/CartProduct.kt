package com.abdallah.ecommerce.data.model

data class CartProduct(
    val product: Product,
    var quantity: Int,
    val color: Int,
    val size: String,
    var isChecked: Boolean = false
) {
    constructor() : this(Product(), 1, 1, "")

}
