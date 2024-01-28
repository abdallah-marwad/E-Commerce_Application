package com.abdallah.ecommerce.ui.fragment.shopping.cart

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abdallah.ecommerce.application.MyApplication
import com.abdallah.ecommerce.data.firebase.FirebaseManager
import com.abdallah.ecommerce.data.model.CartProduct
import com.abdallah.ecommerce.data.model.PlusAndMinus
import com.abdallah.ecommerce.utils.InternetConnection
import com.abdallah.ecommerce.utils.Resource
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CartViewModel @Inject constructor(
    val fireStore: FirebaseFirestore
) : ViewModel() {
    /*
    * I make the _products Channel receiveAsFlow() because when collect it
    * and go to onStop then onStart the collect not fire again
    * */
    private val _products =
        Channel<Resource<List<CartProduct>>>()
    val products: Flow<Resource<List<CartProduct>>> = _products.receiveAsFlow()

    private val _deleteProduct by lazy { Channel<Resource<Boolean>>() }
    val deleteProduct: Flow<Resource<Boolean>> = _deleteProduct.receiveAsFlow()

    private val _changeCartProductCount by lazy { Channel<Resource<PlusAndMinus>>() }
    val changeCartProductCount: Flow<Resource<PlusAndMinus>> =
        _changeCartProductCount.receiveAsFlow()

    private val _noInternet by lazy {   Channel<Boolean>()}
    val noInternet: Flow<Boolean> = _noInternet.receiveAsFlow()

    fun getCartProduct(
        docID: String
    ) {
        viewModelScope.launch { _products.send(Resource.Loading())}
        FirebaseManager.getCartProducts(
            fireStore,
            docID).addOnSuccessListener {
            val cardProducts = it.toObjects(CartProduct::class.java)
            viewModelScope.launch { _products.send(Resource.Success(cardProducts))}
        }.addOnFailureListener {
            viewModelScope.launch {_products.send(Resource.Failure(it.message))}
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
    @RequiresApi(Build.VERSION_CODES.M)
    fun changeCartProductCount(
        docID: String,
        productID: String,
        newValue : Int,
        state : PlusAndMinus
    ) {
        if(!InternetConnection().hasInternetConnection(MyApplication.myAppContext)){
            viewModelScope.launch {_noInternet.send(true)}
            return
        }
        viewModelScope.launch { _changeCartProductCount.send(Resource.Loading())}
        val hashMap = HashMap<String , Any>()
        hashMap["quantity"] = newValue
        FirebaseManager.changeCartProductCount(
            fireStore,
            hashMap,
            docID,
            productID
            ).addOnSuccessListener {
            viewModelScope.launch { _changeCartProductCount.send(Resource.Success(state))}
        }.addOnFailureListener {
            viewModelScope.launch {_changeCartProductCount.send(Resource.Failure(it.message))}
        }
    }

}