package com.abdallah.ecommerce.data.firebase

import com.abdallah.ecommerce.data.model.AddressModel
import com.abdallah.ecommerce.data.model.CartProduct
import com.abdallah.ecommerce.data.model.UserData
import com.abdallah.ecommerce.utils.Constant
import com.google.firebase.firestore.FirebaseFirestore

object FirebaseManager {

    fun getOfferedProducts(
        firestore: FirebaseFirestore,
    ) =
        firestore.collection("products").limit(Constant.HOME_LIMIT_ITEMS)
            .whereEqualTo("hasOffer", true).get()

    fun getProductsByCategory(
        firestore: FirebaseFirestore,
        categoryName: String
    ) =
        firestore.collection("products").whereEqualTo("categoryName", categoryName).get()

    fun getProductsByCategoryPagging(
        firestore: FirebaseFirestore,
        categoryName: String
    ) =
        firestore.collection("products").whereEqualTo("categoryName", categoryName)
            .limit(Constant.PAGE_SIZE)

    fun addReview(
        firestore: FirebaseFirestore,
        hashMap: HashMap<String, Any>,
        docID: String
    ) =
        firestore.collection("products").document(docID).update(hashMap)

    fun addProductToCart(
        firestore: FirebaseFirestore,
        cartList: ArrayList<String>,
        docID: String
    ) =
        firestore.collection("users").document(docID).update("cartProducts", cartList)

    fun addProductToCart(
        firestore: FirebaseFirestore,
        docID: String,
        cartProduct: CartProduct
    ) =
        firestore.collection("users").document(docID).collection("cart")
            .document(cartProduct.product.id).set(cartProduct)

    fun addProductToCart(
        firestore: FirebaseFirestore,
        docID: String,
        productId: String
    ) =
        firestore.collection("users").document(docID).collection("cart").document(productId)

    fun getCartProducts(
        firestore: FirebaseFirestore,
        docID: String,
    ) =
        firestore.collection("users").document(docID).collection("cart").get()

    fun deleteCartItem(
        firestore: FirebaseFirestore,
        docID: String,
        productID: String,
    ) =
        firestore.collection("users").document(docID).collection("cart").document(productID)
            .delete()

    fun changeCartProductCount(
        firestore: FirebaseFirestore,
        hashMap: HashMap<String, Any>,
        docID: String,
        productID: String,
    ) =
        firestore.collection("users").document(docID).collection("cart").document(productID)
            .update(hashMap)

    fun getCartProductCount(
        firestore: FirebaseFirestore,
        docID: String,
    ) =
        firestore.collection("users").document(docID).collection("cart")

    fun getAllProducts(
        firestore: FirebaseFirestore,
    ) = firestore.collection("products").get()

    fun getAllAddresses(
        firestore: FirebaseFirestore,
        docID : String
    ) =  firestore.collection("users").document(docID).collection("addresses").get()
fun addAddress(
        firestore: FirebaseFirestore,
        docID : String,
        address : AddressModel
    ) =  firestore.collection("users").document(docID).collection("addresses").add(address)

}
