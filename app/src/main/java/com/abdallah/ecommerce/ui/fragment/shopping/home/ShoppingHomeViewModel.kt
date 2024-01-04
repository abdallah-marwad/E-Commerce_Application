package com.abdallah.ecommerce.ui.fragment.shopping.home

import android.app.Application
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.abdallah.ecommerce.data.firebase.DownloadImage
import com.abdallah.ecommerce.data.firebase.FirebaseManager
import com.abdallah.ecommerce.data.model.Category
import com.abdallah.ecommerce.data.model.Product
import com.abdallah.ecommerce.utils.Constant
import com.abdallah.ecommerce.utils.Resource
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.google.firebase.storage.ListResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class ShoppingHomeViewModel @Inject constructor(
    application: Application,
    val downloadImage: DownloadImage,
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

    private val _noInternet = Channel<Boolean>()
    val noInternet = _noInternet.receiveAsFlow()


    @RequiresApi(Build.VERSION_CODES.M)
    fun downloadBannerImages() =
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
                    runBlocking { _imageList.emit(Resource.Failure(
                        handleFireBaseException(it)
                    )) }
                }

        }
    private fun handleFireBaseException(exception: java.lang.Exception): String {
        Log.e("test", "$exception")
        if (exception is FirebaseNetworkException) {
            return "Please check your internet"
        }
        return exception.localizedMessage.toString()
    }
    private suspend fun downloadImagesFromListResult(listResult: ListResult): ArrayList<Uri> {
        val arrOFImage = ArrayList<Uri>()
        for (item in listResult.items) {
            val url = item.downloadUrl.await()
            arrOFImage.add(url)
        }
        return arrOFImage
    }

    @RequiresApi(Build.VERSION_CODES.M)
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
                        dataCat.add(Category( categoryName = categoryName, image = categoryImage))
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

    @RequiresApi(Build.VERSION_CODES.M)
    fun getOfferedProducts() = viewModelScope.launch(Dispatchers.IO) {
        _offeredProducts.emit(Resource.Loading())
        try {
            val products = ArrayList<Product>()
            val querySnapshot = FirebaseManager.getOfferedProducts(firestore).await()
            querySnapshot.documents.forEach {
                it.toObject<Product>()?.let { product -> products.add(product) }
            }
            _offeredProducts.emit(Resource.Success(products))
        }catch (e: Exception) {
            _offeredProducts.emit(Resource.Failure(e.message))
        }




    }

}