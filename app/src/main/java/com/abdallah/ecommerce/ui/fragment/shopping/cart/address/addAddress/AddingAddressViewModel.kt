package com.abdallah.ecommerce.ui.fragment.shopping.cart.address.addAddress

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abdallah.ecommerce.data.firebase.FirebaseManager
import com.abdallah.ecommerce.data.firebase.registeration.RegisterWithPhone
import com.abdallah.ecommerce.data.model.AddressModel
import com.abdallah.ecommerce.utils.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthProvider
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
class AddingAddressViewModel @Inject constructor(
    val fireStore: FirebaseFirestore,
    private val phoneRegister: RegisterWithPhone
) : ViewModel() {
    private val _noInternet by lazy { Channel<Boolean>() }
    val noInternet = _noInternet.receiveAsFlow()
    private val _updatePhone = Channel<Resource<Boolean>>()
    val updatePhone = _updatePhone.receiveAsFlow()

    val sendOtpState = phoneRegister.sendOtpState

    private val _addAddress by lazy { Channel<Resource<Boolean>>() }
    val addAddress: Flow<Resource<Boolean>> = _addAddress.receiveAsFlow()

    fun sendVerificationCode(phoneNumber: String, activity: Activity) {
        viewModelScope.launch(Dispatchers.IO) {
            phoneRegister.sendVerificationCode(phoneNumber, activity)
        }
    }

    fun updatePhoneNumber(verificationId: String, smsCode: String) {
        viewModelScope.launch {
            _updatePhone.send(Resource.Loading())
        }
        viewModelScope.launch(Dispatchers.IO) {
            val credential = PhoneAuthProvider.getCredential(verificationId, smsCode)
            FirebaseAuth.getInstance().currentUser?.updatePhoneNumber(credential)
                ?.addOnSuccessListener {
                    viewModelScope.launch {
                        _updatePhone.send(Resource.Success(true))
                    }
                }
                ?.addOnFailureListener {
                    viewModelScope.launch {
                        _updatePhone.send(Resource.Failure(it.message))
                    }
                }
        }
    }

    fun addAddresses(addressID: String, docId: String, addressModel: AddressModel) {
        viewModelScope.launch(Dispatchers.IO) {
            _addAddress.send(Resource.Loading())
        }
        FirebaseManager.addAddress(
            fireStore,
            addressID,
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