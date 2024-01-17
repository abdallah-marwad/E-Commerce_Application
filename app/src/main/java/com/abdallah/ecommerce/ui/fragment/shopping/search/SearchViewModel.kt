package com.abdallah.ecommerce.ui.fragment.shopping.search

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
import dagger.hilt.android.internal.Contexts.getApplication
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    val firestore: FirebaseFirestore
) : ViewModel(
) {

    private val _searchProducts =
        MutableStateFlow<Resource<ArrayList<Product>>>(Resource.UnSpecified())
    val searchProducts: Flow<Resource<ArrayList<Product>>> = _searchProducts
    private val _noInternet = Channel<Boolean>()
    val noInternet = _noInternet.receiveAsFlow()
    @RequiresApi(Build.VERSION_CODES.M)
    fun getProductFromSearch(
        searchWord: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {

            if (!InternetConnection().hasInternetConnection(MyApplication.myAppContext)) {
                _noInternet.send(true)
                return@launch

            }
        }
        viewModelScope.launch(Dispatchers.IO) { _searchProducts.emit(Resource.Loading()) }
        FirebaseManager.searchProducts(
            firestore,
            searchWord
        ).addOnSuccessListener { value ->
                val products = value.toObjects(Product::class.java) as ArrayList<Product>
                viewModelScope.launch(Dispatchers.IO) {
                    _searchProducts.emit(
                        Resource.Success(
                            products
                        )
                    )
            }
        }.addOnFailureListener {
            viewModelScope.launch(Dispatchers.IO) { _searchProducts.emit(Resource.Failure(it.message)) }

        }
    }
}