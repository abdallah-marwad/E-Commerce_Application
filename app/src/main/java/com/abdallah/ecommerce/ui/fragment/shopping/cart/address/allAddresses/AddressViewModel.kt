package com.abdallah.ecommerce.ui.fragment.shopping.cart.address.allAddresses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abdallah.ecommerce.data.firebase.FirebaseManager
import com.abdallah.ecommerce.data.model.AddressModel
import com.abdallah.ecommerce.utils.Resource
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

class AddressViewModel @Inject constructor(
    val fireStore: FirebaseFirestore
) : ViewModel() {
    private val _allAddresses =
        Channel<Resource<ArrayList<AddressModel>>>()
    val allAddresses: Flow<Resource<ArrayList<AddressModel>>> = _allAddresses.receiveAsFlow()

    fun getAllAddresses(docId: String){
        viewModelScope.launch(Dispatchers.IO) {
            _allAddresses.send(Resource.Loading())
        }
        FirebaseManager.getAllAddresses(
            fireStore,
            docId)
            .addOnSuccessListener {
                runBlocking {
                    val address = it.toObjects(AddressModel::class.java) as ArrayList<AddressModel>
                    _allAddresses.send(Resource.Success(address))
                }
            }.addOnFailureListener {
                runBlocking {
                    _allAddresses.send(Resource.Failure(it.message))
                }
            }

    }
}