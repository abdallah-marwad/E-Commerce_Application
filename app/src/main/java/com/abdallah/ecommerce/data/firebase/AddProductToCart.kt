package com.abdallah.ecommerce.data.firebase
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData
import com.abdallah.ecommerce.application.MyApplication
import com.abdallah.ecommerce.data.model.CartProduct
import com.abdallah.ecommerce.data.model.Product
import com.abdallah.ecommerce.utils.InternetConnection
import com.abdallah.ecommerce.utils.Resource
import com.google.android.play.integrity.internal.f
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import dagger.hilt.android.internal.Contexts.getApplication
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

class AddProductToCart @Inject constructor(){
    val addToCartFlow by lazy {
        MutableSharedFlow<Resource<Boolean>>()}
     val noInternet by lazy { MutableLiveData<Boolean>()}


    @RequiresApi(Build.VERSION_CODES.M)
    fun addProductToCartNew(
            docID: String,
            product: Product,
            selectedColor: Int,
            selectedSize: String,
            fireStore: FirebaseFirestore

        ) {

        if (!InternetConnection().hasInternetConnection(MyApplication.myAppContext)) {
            noInternet.value = true
            return
        }
            runBlocking { addToCartFlow.emit(Resource.Loading()) }
            val newCartList = CartProduct(
                product,
                1,
                selectedColor,
                selectedSize

            )
            val docRef = FirebaseManager.addProductToCart(fireStore, docID, product.id)
            fireStore.runTransaction { transaction ->
                val snapshot = transaction.get(docRef)
                if (snapshot.exists()) {
                    val cartObj = snapshot.toObject(CartProduct::class.java)
                    val newQuantity = cartObj!!.quantity + 1
                    val updatedData: MutableMap<String, Any> = HashMap()
                    updatedData["quantity"] = newQuantity
                    updatedData["color"] = selectedColor
                    updatedData["size"] = selectedSize
                    transaction.set(docRef, updatedData, SetOptions.merge())
                    null
                } else {
                    transaction.set(docRef, newCartList)
                    null
                }
            }.addOnSuccessListener {
                runBlocking {
                    addToCartFlow.emit(Resource.Success(true))
                }
            }.addOnFailureListener {
                runBlocking {
                    addToCartFlow.emit(Resource.Failure(it.message))
                }

            }

        }
    }