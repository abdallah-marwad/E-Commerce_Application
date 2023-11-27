package com.abdallah.ecommerce.data.firebase

import com.abdallah.ecommerce.utils.Resource
import com.google.firebase.Firebase
import com.google.firebase.storage.ListResult
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.storage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

class DownloadImage @Inject constructor(private val imageRef : StorageReference ) {


     fun downloadAllImages(path : String) =
          imageRef.child(path).listAll()
     fun downloadImage(path : String) =
          imageRef.child(path).downloadUrl


}
