package com.abdallah.ecommerce.ui.fragment.shopping.productDetails.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abdallah.ecommerce.data.firebase.FirebaseManager
import com.abdallah.ecommerce.data.model.Product
import com.abdallah.ecommerce.data.model.RatingModel
import com.abdallah.ecommerce.utils.Resource
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

class ProductDetailsViewModel @Inject constructor(
    val fireStore: FirebaseFirestore
) : ViewModel() {

    private val _review =
        MutableStateFlow<Resource<Boolean>>(Resource.UnSpecified())
    val review: Flow<Resource<Boolean>> = _review

    fun addReview(numOfRating: Int, ratingNum: Float, newRating: Int, docID: String) {
        val ratingModel = generateReviewModel(numOfRating, ratingNum, newRating)
        viewModelScope.launch(Dispatchers.IO) {
            _review.emit(Resource.Loading())
            FirebaseManager.addReview(fireStore, ratingModel, docID)
                .addOnSuccessListener {
                    runBlocking {
                    _review.emit(Resource.Success(true))
                    }
                }.addOnFailureListener {
                    runBlocking {
                        _review.emit(Resource.Failure(it.message))
                    }
                }
        }
    }

    private fun generateReviewModel(
        numOfRating: Int,
        ratingNum: Float,
        newRating: Int
    ): RatingModel {
        val totalRatings = numOfRating + 1
        val sumOfRatings: Float = (numOfRating * ratingNum) + (newRating)
        val overallRating = sumOfRatings / totalRatings

        return RatingModel()

    }

}