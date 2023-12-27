package com.abdallah.ecommerce.ui.fragment.shopping.home

import android.app.Application
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abdallah.ecommerce.data.firebase.DownloadImage
import com.abdallah.ecommerce.data.firebase.FirebaseManager
import com.abdallah.ecommerce.data.model.Category
import com.abdallah.ecommerce.data.model.Product
import com.abdallah.ecommerce.utils.Constant
import com.abdallah.ecommerce.utils.Resource
import com.google.android.gms.tasks.Tasks.await
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.google.firebase.storage.ListResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ShoppingHomeViewModel @Inject constructor(
    application: Application,
    val downloadImage: DownloadImage,
    val firebaseManager: FirebaseManager,
    val firestore: FirebaseFirestore

) : AndroidViewModel(application) {

    private val _imageList = MutableStateFlow<Resource<ArrayList<Uri>>>(Resource.UnSpecified())
    val imageList: Flow<Resource<ArrayList<Uri>>> = _imageList
    private val _categoryList =
        MutableStateFlow<Resource<ArrayList<Category>>>(Resource.UnSpecified())
    val categoryList: Flow<Resource<ArrayList<Category>>> = _categoryList
    private val _offeredProducts =
        MutableStateFlow<Resource<ArrayList<Product>>>(Resource.UnSpecified())
    val offeredProducts: Flow<Resource<ArrayList<Product>>> = _offeredProducts


    fun downloadAllImages() =
        viewModelScope.launch(Dispatchers.IO) {
            _imageList.emit(Resource.Loading())

            downloadImage.downloadAllImages(Constant.HOME_BANNER_BATH)
                .addOnSuccessListener { result ->
                    result?.let {
                        runBlocking {
                            _imageList.emit(
                                Resource.Success(
                                    downloadImagesFromListResult(
                                        it
                                    )
                                )
                            )
                        }
                    }
                }
                .addOnFailureListener {
                    runBlocking { _imageList.emit(Resource.Failure(it.message)) }
                }

        }

    private suspend fun downloadImagesFromListResult(listResult: ListResult): ArrayList<Uri> {
        val arrOFImage = ArrayList<Uri>()
        for (item in listResult.items) {
            item.downloadUrl
            val url = item.downloadUrl.await()
            arrOFImage.add(url)
        }
        return arrOFImage
    }

    fun getCategories() = viewModelScope.launch(Dispatchers.IO) {
        _categoryList.emit(Resource.Loading())

        val collectionReference = firestore.collection("category")
        collectionReference.get()
            .addOnSuccessListener { document ->

                if (!document.isEmpty) {
                    val dataCat = ArrayList<Category>()
                    document.forEach {
                        val data = it.data
                        val categoryName = data["categoryName"] as String?
                        val categoryImage = data["image"] as String?
                        dataCat.add(Category(categoryName, categoryImage))
                    }
                    runBlocking {
                        _categoryList.emit(Resource.Success(dataCat))
                    }
                }
            }
            .addOnFailureListener { exception ->
                runBlocking {
                    _categoryList.emit(Resource.Failure(exception.message))
                }

            }


    }

    fun getOfferedProducts() = viewModelScope.launch(Dispatchers.IO) {
        _offeredProducts.emit(Resource.Loading())
        try {
            val products = ArrayList<Product>()
            val querySnapshot = firebaseManager.getProduct(firestore).await()
            querySnapshot.documents.forEach {
                it.toObject<Product>()?.let { product -> products.add(product) }
            }
            _offeredProducts.emit(Resource.Success(products))
        }catch (e: Exception) {
            _offeredProducts.emit(Resource.Failure(e.message))
        }




    }

}