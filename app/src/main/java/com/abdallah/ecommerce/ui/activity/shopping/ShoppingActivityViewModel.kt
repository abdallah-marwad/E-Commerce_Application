package com.abdallah.ecommerce.ui.activity.shopping

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abdallah.ecommerce.data.firebase.FirebaseManager
import com.abdallah.ecommerce.data.model.CartProduct
import com.abdallah.ecommerce.data.model.Category
import com.abdallah.ecommerce.utils.Resource
import com.bumptech.glide.Glide.init
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObjects
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShoppingActivityViewModel @Inject constructor(
    val firestore: FirebaseFirestore
): ViewModel(){
    private val _cartSize =
        MutableStateFlow<Resource<Int>>(Resource.UnSpecified())
    val cartSize: Flow<Resource<Int>> = _cartSize
    fun getItemsInCart(
        docID : String
    ) {
        viewModelScope.launch (Dispatchers.IO){_cartSize.emit(Resource.Loading())  }
        FirebaseManager.getCartProductCount(
            firestore,
            docID
        ).addSnapshotListener { value, error ->
            if (value != null) {
                val products = value.toObjects(CartProduct::class.java)
                viewModelScope.launch (Dispatchers.IO){_cartSize.emit(Resource.Success(products?.size ?:0))  }
            } else if (error != null && error.message.isNullOrBlank()) {
                viewModelScope.launch (Dispatchers.IO){_cartSize.emit(Resource.Failure(error.message))  }

            }
        }
    }
}