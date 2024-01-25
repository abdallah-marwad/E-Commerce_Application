package com.abdallah.ecommerce.ui.fragment.shopping.home

import android.app.Application
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.abdallah.ecommerce.data.firebase.AddProductToCart
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
    val firestore: FirebaseFirestore,
    val addProductToCart : AddProductToCart
) : AndroidViewModel(application) {


    val addToCartFlow = addProductToCart.addToCartFlow
    val noInternetAddProduct = addProductToCart.noInternet
    private val _imageList = Channel<Resource<ArrayList<Uri>>>()
    val imageList: Flow<Resource<ArrayList<Uri>>> = _imageList.receiveAsFlow()
    private val _categoryList =
        Channel<Resource<ArrayList<Category>>>()
    val categoryList: Flow<Resource<ArrayList<Category>>> = _categoryList.receiveAsFlow()
    private val _offeredProducts =
        Channel<Resource<ArrayList<Product>>>()
    val offeredProducts: Flow<Resource<ArrayList<Product>>> = _offeredProducts.receiveAsFlow()

    private val _noInternet = Channel<Boolean>()
    val noInternet = _noInternet.receiveAsFlow()


    @RequiresApi(Build.VERSION_CODES.M)
    fun downloadBannerImages() {
        viewModelScope.launch(Dispatchers.IO) {
            _imageList.send(Resource.Loading())}
            downloadImage.downloadAllImages(Constant.HOME_BANNER_BATH)
                .addOnSuccessListener { result ->
                    result?.let {
                        viewModelScope.launch(Dispatchers.IO) {
                            _imageList.send(
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
                    runBlocking { _imageList.send(Resource.Failure(
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
    fun getCategories() {
        viewModelScope.launch(Dispatchers.IO) {
            _categoryList.send(Resource.Loading())
        }
        val collectionReference = firestore.collection("category")
        collectionReference.get()
            .addOnSuccessListener { document ->
                if (!document.isEmpty) {
                    var dataCat: ArrayList<Category> = document.toObjects(Category::class.java) as ArrayList<Category>
                    viewModelScope.launch(Dispatchers.IO) {
                        _categoryList.send(Resource.Success(dataCat))
                    }
                }
            }
            .addOnFailureListener { exception ->
                viewModelScope.launch(Dispatchers.IO) {
                    _categoryList.send(Resource.Failure(exception.message))
                }
            }


    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun getOfferedProducts() = viewModelScope.launch(Dispatchers.IO) {
        _offeredProducts.send(Resource.Loading())
        try {
            val products = ArrayList<Product>()
            val querySnapshot = FirebaseManager.getOfferedProducts(firestore).await()
            querySnapshot.documents.forEach {
                it.toObject<Product>()?.let { product -> products.add(product) }
            }
            _offeredProducts.send(Resource.Success(products))
        }catch (e: Exception) {
            _offeredProducts.send(Resource.Failure(e.message))
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