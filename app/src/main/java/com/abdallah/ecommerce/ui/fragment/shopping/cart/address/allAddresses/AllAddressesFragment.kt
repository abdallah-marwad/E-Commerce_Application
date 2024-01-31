package com.abdallah.ecommerce.ui.fragment.shopping.cart.address.allAddresses

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.abdallah.ecommerce.application.core.BaseFragment
import com.abdallah.ecommerce.data.model.AddressModel
import com.abdallah.ecommerce.databinding.FragmentAllAddresesBinding
import com.abdallah.ecommerce.utils.Constant
import com.abdallah.ecommerce.utils.Resource
import com.abdallah.ecommerce.utils.animation.RecyclerAnimation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AllAddressesFragment : BaseFragment<FragmentAllAddresesBinding>(),
    AllAddressesAdapter.AddressOnClick {
    @Inject
    lateinit var firestore: FirebaseFirestore
    var adapter: AllAddressesAdapter? = null
    private var selectedAddress: AddressModel? = null
    private var selectedAddressID: String? = null
    private var registerForAllAddresses = true
    private var registerForSelectedAddresses = true

    @Inject
    lateinit var firebaseAuth: FirebaseAuth
    private val viewModel by viewModels<AllAddressViewModel>()
    private val callback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            binding.done.performClick()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        getAddressesCallBack()
        getSelectedAddressCallBack()
        getSelectedAddresses()
        fragOnClick()
        getAddressFromAddAddressFrag()
        updateSelectedAddressesCallBack()
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }

    private fun initViews() {
        binding.appbar.title.text = "All Addresses"
    }

    private fun getAddressFromAddAddressFrag() {
        findNavController().currentBackStackEntry?.savedStateHandle
            ?.getLiveData<AddressModel>(Constant.ADDRESS)?.observe(viewLifecycleOwner) { result ->
                binding.noAddresses.visibility = View.GONE
                if (adapter == null) {
                    val list = ArrayList<AddressModel>()
                    list.add(result)
                    initAddressRV(list)
                    return@observe
                }
                adapter!!.data.add(result)
                adapter!!.notifyDataSetChanged()

            }
    }

    private fun fragOnClick() {
        binding.addAddress.setOnClickListener {
            findNavController().navigate(com.abdallah.ecommerce.R.id.action_allAddressesFragment_to_addAddressFragment)
        }
        binding.appbar.cardImage.setOnClickListener {
            binding.done.performClick()
        }
        binding.done.setOnClickListener {
            if (selectedAddress == null) {
                findNavController().popBackStack()
                return@setOnClickListener
            }

            // here don't return the address because it already got in cartFrag
            if (selectedAddress!!.id == selectedAddressID) {
                findNavController().popBackStack()
                return@setOnClickListener
            }
            updateSelectedAddress(selectedAddress!!)
        }
    }

    private fun updateSelectedAddress(selectedAddress: AddressModel) {
        viewModel.addSelectedAddress(firebaseAuth.currentUser!!.uid, selectedAddress)
    }

    private fun updateSelectedAddressesCallBack() {
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.addAddress.collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            hideProgressDialog()
                            findNavController()
                                .previousBackStackEntry?.savedStateHandle?.set(
                                    Constant.ADDRESS,
                                    selectedAddress
                                )
                            findNavController().popBackStack()
                        }

                        is Resource.Failure -> {
                            hideProgressDialog()
                            showLongToast(result.message ?: "")
                            findNavController().popBackStack()
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

    private fun getAllAddresses() {
        if (registerForAllAddresses.not())
            return
        viewModel.getAllAddresses(firebaseAuth.currentUser!!.uid)
    }

    private fun getAddressesCallBack() {
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.allAddresses.collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            registerForAllAddresses = false
                            if (result.data!!.isEmpty()) {
                                binding.noAddresses.visibility = View.VISIBLE
                                findNavController().navigate(com.abdallah.ecommerce.R.id.action_allAddressesFragment_to_addAddressFragment)
                                hideProgressDialog()

                                return@collect
                            }
                            hideProgressDialog()
                            binding.noAddresses.visibility = View.GONE
                            initAddressRV(result.data)
                        }

                        is Resource.Failure -> {
                            hideProgressDialog()
                            showLongToast(result.message ?: "")
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

    private fun getSelectedAddresses() {
        if (registerForSelectedAddresses.not())
            return
        viewModel.getSelectedAddress(firebaseAuth.currentUser!!.uid)
    }

    private fun getSelectedAddressCallBack() {
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.selectedAddress.collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            hideProgressDialog()
                            registerForSelectedAddresses = false
                            selectedAddress = result.data
                            selectedAddressID = selectedAddress?.id
                            getAllAddresses()
                        }

                        is Resource.Failure -> {
                            hideProgressDialog()
                            getAllAddresses()
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
        adapter = AllAddressesAdapter(list, this)
        binding.rvAddresses.adapter = adapter
        RecyclerAnimation.animateRecycler(binding.rvAddresses)
        adapter!!.notifyDataSetChanged()
        binding.rvAddresses.scheduleLayoutAnimation()
        selectAddress(list)
    }

    private fun selectAddress(list: ArrayList<AddressModel>) {
        if (selectedAddress == null) {
            adapter!!.selectSpecificItem(0)
            selectedAddress = list[0]
            return
        }
        val selectedAddressFiltered = adapter!!.data.filter { it.id == selectedAddress!!.id }
        if (selectedAddressFiltered.isNotEmpty()) {
            val index = adapter!!.data.indexOf(selectedAddressFiltered[0])
            adapter!!.selectSpecificItem(index)

        }


    }

    override fun onClick(address: AddressModel) {
        selectedAddress = address
    }
}