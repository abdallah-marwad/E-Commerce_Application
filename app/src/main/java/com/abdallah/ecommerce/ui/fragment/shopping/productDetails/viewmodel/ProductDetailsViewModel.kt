package com.abdallah.ecommerce.ui.fragment.shopping.productDetails.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abdallah.ecommerce.data.firebase.AddProductToCart
import com.abdallah.ecommerce.data.firebase.FirebaseManager
import com.abdallah.ecommerce.data.model.Product
import com.abdallah.ecommerce.data.model.RatingModel
import com.abdallah.ecommerce.utils.Resource
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
import java.util.Date
import javax.inject.Inject


@HiltViewModel
class ProductDetailsViewModel @Inject constructor(
    val fireStore: FirebaseFirestore,
    val addProductToCart : AddProductToCart
) : ViewModel() {

    val addToCartFlow = addProductToCart.addToCartFlow
    val review by lazy {
        MutableStateFlow<Resource<HashMap<String, Any>>>(Resource.UnSpecified())
    }
    var favouriteList: ArrayList<String>? = null
    var productCartList: ArrayList<String>? = null
    var email: String = ""


    fun addReview(
        ratersNum: Int,
        currentRating: Float,
        newRating: Int,
        docID: String,
        comment: String,
        listRating: ArrayList<RatingModel>,
        email: String,
        name: String
    ) {
        val ratingModel = generateReviewUpdatesMap(
            ratersNum,
            currentRating,
            newRating,
            comment,
            listRating,
            email,
            name
        )
        viewModelScope.launch(Dispatchers.IO) {
            review.emit(Resource.Loading())
            FirebaseManager.addReview(fireStore, ratingModel, docID)
                .addOnSuccessListener {
                    runBlocking {
                        review.emit(Resource.Success(ratingModel))
                    }
                }.addOnFailureListener {
                    runBlocking {
                        review.emit(Resource.Failure(it.message))
                    }
                }
        }
    }

    private fun generateReviewUpdatesMap(
        ratersNum: Int,
        currentRating: Float,
        newRating: Int,
        comment: String,
        listRating: ArrayList<RatingModel>,
        email: String,
        name: String
    ): HashMap<String, Any> {
        val totalRatings = ratersNum + 1
        val sumOfRatings: Float = (ratersNum * currentRating) + (newRating)
        val overallRating = sumOfRatings / totalRatings
        var mutableListRating = listRating
        val outputFormatter = SimpleDateFormat("MM/dd/yyyy")
        val formattedDate: String = outputFormatter.format(Date())
        val ratingModel = RatingModel(
            rating = newRating.toFloat(),
            comment = comment,
            date = formattedDate,
            raterId = email,
            raterName = name
        )
        mutableListRating.add(ratingModel)
        return hashMapOf(
            "rating" to overallRating,
            "ratingList" to mutableListRating,
            "ratersNum" to totalRatings


        )

    }
    fun addProductToCart(
        docID: String,
        product: Product,
        selectedColor: Int,
        selectedSize: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            addProductToCart.addProductToCartNew(
                docID,
                product,
                selectedColor,
                selectedSize,
                fireStore
            )
        }
    }

}