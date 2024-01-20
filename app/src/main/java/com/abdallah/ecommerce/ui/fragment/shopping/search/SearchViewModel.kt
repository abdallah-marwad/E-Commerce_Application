package com.abdallah.ecommerce.ui.fragment.shopping.search

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abdallah.ecommerce.application.MyApplication
import com.abdallah.ecommerce.data.firebase.AddProductToCart
import com.abdallah.ecommerce.data.firebase.FirebaseManager
import com.abdallah.ecommerce.data.model.Product
import com.abdallah.ecommerce.utils.InternetConnection
import com.abdallah.ecommerce.utils.Resource
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    val firestore: FirebaseFirestore,
    val addProductToCart: AddProductToCart

) : ViewModel(
) {
    var products: ArrayList<Product>? = null
    val addToCartFlow = addProductToCart.addToCartFlow
    val noInternetCart = addProductToCart.noInternet

    private val _searchProducts =
        MutableLiveData<Resource<ArrayList<Product>>>(Resource.UnSpecified())
    val searchProducts: LiveData<Resource<ArrayList<Product>>> = _searchProducts
    private val _noInternet = Channel<Boolean>()
    val noInternet = _noInternet.receiveAsFlow()

    @RequiresApi(Build.VERSION_CODES.M)
    fun getProductFromSearch() {
        viewModelScope.launch(Dispatchers.IO) {
            if (!InternetConnection().hasInternetConnection(MyApplication.myAppContext)) {
                _noInternet.send(true)
                return@launch

            }
        }
        _searchProducts.postValue(Resource.Loading())
        FirebaseManager.getAllProducts(
            firestore,
        ).addOnSuccessListener { value ->
            val products = value.toObjects(Product::class.java) as ArrayList<Product>
            _searchProducts.postValue(
                Resource.Success(
                    products
                )
            )
        }.addOnFailureListener {
            _searchProducts.postValue(Resource.Failure(it.message))

        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun addProductToCart(
        docID: String,
        product: Product,
        selectedColor: Int,
        selectedSize: String
    ) {
        addProductToCart.addProductToCartNew(
            docID,
            product,
            selectedColor,
            selectedSize,
            firestore
        )

    }
}