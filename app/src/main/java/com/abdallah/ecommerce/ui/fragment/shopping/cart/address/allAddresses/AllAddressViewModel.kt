package com.abdallah.ecommerce.ui.fragment.shopping.cart.address.allAddresses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abdallah.ecommerce.data.firebase.FirebaseManager
import com.abdallah.ecommerce.data.model.AddressModel
import com.abdallah.ecommerce.utils.Resource
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltViewModel
class AllAddressViewModel @Inject constructor(
    val fireStore: FirebaseFirestore
) : ViewModel() {
    private val _allAddresses by lazy {
        Channel<Resource<ArrayList<AddressModel>>>()
    }
    val allAddresses: Flow<Resource<ArrayList<AddressModel>>> by lazy { _allAddresses.receiveAsFlow() }

    private val _addAddress by lazy { Channel<Resource<Boolean>>() }
    val addAddress: Flow<Resource<Boolean>> by lazy { _addAddress.receiveAsFlow() }

    private val _selectedAddress by lazy { Channel<Resource<AddressModel>>() }
    val selectedAddress: Flow<Resource<AddressModel>> by lazy { _selectedAddress.receiveAsFlow() }
    fun getAllAddresses(docId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _allAddresses.send(Resource.Loading())
        }
        FirebaseManager.getAllAddresses(
            fireStore,
            docId
        )
            .addOnSuccessListener {
                viewModelScope.launch(Dispatchers.IO) {
                    val address = it.toObjects(AddressModel::class.java) as ArrayList<AddressModel>
                    _allAddresses.send(Resource.Success(address))
                }
            }.addOnFailureListener {
                viewModelScope.launch(Dispatchers.IO) {
                    _allAddresses.send(Resource.Failure(it.message))
                }
            }
    }

    fun getSelectedAddress(docId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _selectedAddress.send(Resource.Loading())
        }
        FirebaseManager.getSelectedAddress(
            fireStore,
            docId,
        ).addOnSuccessListener {
            viewModelScope.launch(Dispatchers.IO) {
                val selectedAddress = it.toObject(AddressModel::class.java)
                _selectedAddress.send(Resource.Success(selectedAddress))
            }
        }.addOnFailureListener {
            runBlocking {
                _selectedAddress.send(Resource.Failure(it.message))
            }
        }
    }

    fun addSelectedAddress(docId: String, addressModel: AddressModel) {
        viewModelScope.launch(Dispatchers.IO) {
            _addAddress.send(Resource.Loading())
        }
        FirebaseManager.updateSelectedAddress(
            fireStore,
            docId,
            addressModel
        ).addOnSuccessListener {
            viewModelScope.launch(Dispatchers.IO) {
                _addAddress.send(Resource.Success(true))
            }
        }.addOnFailureListener {
            runBlocking {
                _addAddress.send(Resource.Failure(it.message))
            }
        }
    }
}