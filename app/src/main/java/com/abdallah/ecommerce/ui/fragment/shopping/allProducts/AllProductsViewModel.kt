package com.abdallah.ecommerce.ui.fragment.shopping.allProducts

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.abdallah.ecommerce.data.firebase.FirebaseManager
import com.abdallah.ecommerce.data.firebase.FirebasePagingSource
import com.abdallah.ecommerce.data.model.Product
import com.abdallah.ecommerce.utils.Constant
import com.abdallah.ecommerce.utils.Resource
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class AllProductsViewModel @Inject constructor(
    val fireStore : FirebaseFirestore
): ViewModel() {

     var categoryName = ""
    private val _products =
        MutableStateFlow<Resource<ArrayList<Product>>>(Resource.UnSpecified())
    val productsFlow: Flow<Resource<ArrayList<Product>>> = _products
    val flow = Pager(PagingConfig(pageSize = Constant.PAGE_SIZE.toInt())) {
        FirebasePagingSource(FirebaseManager.getProductsByCategoryPagging(fireStore , categoryName))
    }.flow.cachedIn(viewModelScope)

    @RequiresApi(Build.VERSION_CODES.M)
    fun getProductsByCategory(categoryName : String) =
        viewModelScope.launch(Dispatchers.IO) {
            _products.emit(Resource.Loading())
        try {
            val products = ArrayList<Product>()
            val querySnapshot = FirebaseManager.getProductsByCategory(fireStore , categoryName).await()
            querySnapshot.documents.forEach {
                it.toObject<Product>()?.let { product -> products.add(product) }
            }
            _products.emit(Resource.Success(products))
        }catch (e: Exception) {
            _products.emit(Resource.Failure(e.message))
        }




    }

}