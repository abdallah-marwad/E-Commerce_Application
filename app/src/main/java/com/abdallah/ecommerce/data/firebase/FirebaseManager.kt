package com.abdallah.ecommerce.data.firebase

import com.abdallah.ecommerce.data.model.CartProduct
import com.abdallah.ecommerce.data.model.Product
import com.abdallah.ecommerce.data.model.RatingModel
import com.abdallah.ecommerce.data.model.UserData
import com.abdallah.ecommerce.utils.Constant

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import javax.inject.Inject

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

    fun getUserData(
        firestore: FirebaseFirestore,
        email: String
    ) =
        firestore.collection("users").whereEqualTo("email", email)

    fun saveUserData(
        firestore: FirebaseFirestore,
        userData: UserData,
    ) =
        firestore.collection("users").document(userData.email).set(userData)

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

    fun getProductFromSearch(
        firestore: FirebaseFirestore,
        searchWord: String,
    ) = firestore.collection("products").whereIn("productName", listOf(searchWord)).get()

    fun searchProducts(
        firestore: FirebaseFirestore,
        searchQuery: String
    ) = firestore.collection("products")
        .orderBy("productName")
        .startAt(searchQuery)
        .endAt("\u03A9+$searchQuery")
        .get()
}
