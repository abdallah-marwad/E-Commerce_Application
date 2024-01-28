package com.abdallah.ecommerce.ui.fragment.shopping.cart.address.allAddresses

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.abdallah.ecommerce.R
import com.abdallah.ecommerce.application.core.BaseFragment
import com.abdallah.ecommerce.data.model.AddressModel
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
                            if (result.data!!.isEmpty())
                                findNavController().navigate(R.id.action_allAddressesFragment_to_addAddressFragment)
                            initAddressRV(result.data)
                            hideProgressDialog()
                        }
                        is Resource.Failure -> {
                            hideProgressDialog()
                            showShortSnackBar(result.message ?: "")
                        }

                        is Resource.Loading -> {
                            showProgressDialog()
                        }

                        else -> {}
                    }
                }
            }
        }
    }

    private fun initAddressRV(list: ArrayList<AddressModel>) {
//        val adapter = CartRVAdapter(cartList.toMutableList(), this)
//        binding.rvCart.adapter = adapter
//        RecyclerAnimation.animateRecycler(binding.rvCart)
//        adapter.notifyDataSetChanged()
//        binding.rvCart.scheduleLayoutAnimation()
//        val itemTouchHelper = ItemTouchHelper(onSwipe())
//        itemTouchHelper.attachToRecyclerView(binding.rvCart)
    }
}