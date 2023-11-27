package com.abdallah.ecommerce.ui.fragment.shopping.home

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abdallah.ecommerce.data.firebase.DownloadImage
import com.abdallah.ecommerce.utils.Constant
import com.abdallah.ecommerce.utils.Resource
import com.google.android.gms.tasks.Tasks.await
import com.google.firebase.storage.ListResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class ShoppingHomeViewModel @Inject constructor(
    application: Application,
    val downloadImage: DownloadImage

) : AndroidViewModel(application) {

    private val _imageList = MutableStateFlow<Resource<ArrayList<Uri>>>(Resource.UnSpecified())
    val imageList: Flow<Resource<ArrayList<Uri>>> = _imageList


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
}