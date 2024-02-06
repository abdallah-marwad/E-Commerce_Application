package com.abdallah.ecommerce.data.firebase
import android.os.Build
import androidx.annotation.RequiresApi
import com.abdallah.ecommerce.application.MyApplication
import com.abdallah.ecommerce.data.model.CartProduct
import com.abdallah.ecommerce.data.model.Product
import com.abdallah.ecommerce.utils.InternetConnection
import com.abdallah.ecommerce.utils.Resource
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

class AddProductToCart @Inject constructor() {
    private val _addToCartFlow by lazy {
        Channel<Resource<Boolean>>()
    }
    val addToCartFlow: Flow<Resource<Boolean>> = _addToCartFlow.receiveAsFlow()

    private val _noInternet by lazy { Channel<Boolean>() }
    val noInternet: Flow<Boolean> = _noInternet.receiveAsFlow()

    @RequiresApi(Build.VERSION_CODES.M)
    fun addProductToCartNew(
        docID: String,
        product: Product,
        selectedColor: Int,
        selectedSize: String,
        fireStore: FirebaseFirestore

    ) {
        if (!InternetConnection().hasInternetConnection(MyApplication.myAppContext)) {
            runBlocking { _noInternet.send(true) }
            return
        }
        runBlocking { _addToCartFlow.send(Resource.Loading()) }
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
                    val newCartList = CartProduct(
                        product,
                        1,
                        selectedColor,
                        selectedSize
                    )
                    transaction.set(docRef, newCartList)
                    null
                }
            }.addOnSuccessListener {
                runBlocking {
                    _addToCartFlow.send(Resource.Success(true))
                }
            }.addOnFailureListener {
                runBlocking {
                    _addToCartFlow.send(Resource.Failure(it.message))
                }
            }
        }
    }