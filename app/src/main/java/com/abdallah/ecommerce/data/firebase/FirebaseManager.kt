package com.abdallah.ecommerce.data.firebase

import com.abdallah.ecommerce.data.model.Product
import com.abdallah.ecommerce.utils.Constant

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import javax.inject.Inject

object FirebaseManager{

    fun getOfferedProducts(
        firestore: FirebaseFirestore,
    )=
         firestore.collection("products").limit(Constant.HOME_LIMIT_ITEMS).whereEqualTo( "hasOffer", true).get()

 fun getProductsByCategory(
        firestore: FirebaseFirestore,
        categoryName : String
    )=
         firestore.collection("products").whereEqualTo( "categoryName", categoryName).get()

fun getProductsByCategoryPagging(
        firestore: FirebaseFirestore,
        categoryName : String
    )=
         firestore.collection("products").whereEqualTo( "categoryName", categoryName).limit(Constant.PAGE_SIZE)
fun getUserData(
        firestore: FirebaseFirestore,
        categoryName : String,
        email : String
    )=
         firestore.collection("users").whereEqualTo( "email", email)




}