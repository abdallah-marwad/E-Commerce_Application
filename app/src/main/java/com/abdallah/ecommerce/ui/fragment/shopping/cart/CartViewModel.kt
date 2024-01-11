package com.abdallah.ecommerce.ui.fragment.shopping.cart

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abdallah.ecommerce.application.MyApplication
import com.abdallah.ecommerce.data.firebase.FirebaseManager
import com.abdallah.ecommerce.data.model.CartProduct
import com.abdallah.ecommerce.data.model.Product
import com.abdallah.ecommerce.utils.InternetConnection
import com.abdallah.ecommerce.utils.Resource
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObjects
import dagger.hilt.android.internal.Contexts.getApplication
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CartViewModel @Inject constructor(
    val fireStore: FirebaseFirestore
) : ViewModel() {
    private val _products =
        MutableStateFlow<Resource<List<CartProduct>>>(Resource.UnSpecified())
    val products: Flow<Resource<List<CartProduct>>> = _products

    private val _deleteProduct by lazy {   Channel<Resource<Boolean>>()}
    val deleteProduct: Flow<Resource<Boolean>> = _deleteProduct.receiveAsFlow()

    private val _noInternet by lazy {   Channel<Boolean>()}
    val noInternet: Flow<Boolean> = _noInternet.receiveAsFlow()

    fun getCartProduct(
        docID: String
    ) {
        viewModelScope.launch { _products.emit(Resource.Loading())}
        FirebaseManager.getCartProducts(
            fireStore,
            docID).addOnSuccessListener {
            val cardProducts = it.toObjects(CartProduct::class.java)
            viewModelScope.launch { _products.emit(Resource.Success(cardProducts))}
        }.addOnFailureListener {
            viewModelScope.launch {_products.emit(Resource.Failure(it.message))}
        }
    }
    @RequiresApi(Build.VERSION_CODES.M)
    fun deleteProduct(
        docID: String,
        productID: String
    ) {
        if(!InternetConnection().hasInternetConnection(MyApplication.myAppContext)){
            viewModelScope.launch {_noInternet.send(true)}
            return
        }
        viewModelScope.launch { _deleteProduct.send(Resource.Loading())}
        FirebaseManager.deleteCartItem(
            fireStore,
            docID,
            productID
            ).addOnSuccessListener {
            viewModelScope.launch { _deleteProduct.send(Resource.Success(true))}
        }.addOnFailureListener {
            viewModelScope.launch {_deleteProduct.send(Resource.Failure(it.message))}
        }
    }

}