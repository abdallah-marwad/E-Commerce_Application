package com.abdallah.ecommerce.ui.fragment.shopping.cart.address

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.abdallah.ecommerce.R
import com.abdallah.ecommerce.application.core.BaseFragment
import com.abdallah.ecommerce.databinding.FragmentAllAddresesBinding
import com.abdallah.ecommerce.utils.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class AllAddressesFragment : BaseFragment<FragmentAllAddresesBinding>() {
    @Inject
    lateinit var firestore: FirebaseFirestore

    @Inject
    lateinit var firebaseAuth: FirebaseAuth
     var registerForAddress = true
    private val viewModel by viewModels<AddressViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getAddressesCallBack()
        getAllAddresses()
    }

    private fun getAllAddresses() {
        viewModel.getAllAddresses(firebaseAuth.currentUser!!.uid)
    }
    private fun getAddressesCallBack() {
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.allAddresses.collect { result ->
                    when (result) {
                        is Resource.Success -> {


                        }
                        is Resource.Failure -> {

                        }

                        is Resource.Loading -> {
                        }

                        else -> {}
                    }
                }
            }
        }
    }
}