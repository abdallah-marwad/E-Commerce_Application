package com.abdallah.ecommerce.ui.fragment.shopping.cart

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import com.abdallah.ecommerce.R
import com.abdallah.ecommerce.application.core.BaseFragment
import com.abdallah.ecommerce.data.model.AddressModel
import com.abdallah.ecommerce.data.model.CartProduct
import com.abdallah.ecommerce.data.model.PlusAndMinus
import com.abdallah.ecommerce.databinding.FragmentCartBinding
import com.abdallah.ecommerce.utils.Constant
import com.abdallah.ecommerce.utils.Resource
import com.abdallah.ecommerce.utils.animation.RecyclerAnimation
import com.abdallah.ecommerce.utils.animation.RvSwipe
import com.abdallah.ecommerce.utils.dialogs.AppDialog
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class CartFragment : BaseFragment<FragmentCartBinding>(), CartRVAdapter.CartOnClick {

    private val viewModel by viewModels<CartViewModel>()
    lateinit var adapter: CartRVAdapter
    private var itemPrice = 0.0
    private var posToDelete: Int = 0
    private var posToChange: Int = 0
    private var isChecked = false
    private var registerForCart = true
    private val appDialog by lazy { AppDialog() }
    private var selectedAddress: AddressModel? = null
    private var registerForSelectedAddresses = true


    @Inject
    lateinit var firebaseAuth: FirebaseAuth

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        getProductDataCallback()
        deleteProductCallback()
        changeProductCountCallback()
        getProductData()
        noInternetCallback()
        fragOnClick()
        getAddressFromAllAddressFrag()
        getSelectedAddressCallBack()

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
                            binding.locationProgress.visibility = View.GONE
                            registerForSelectedAddresses = false
                            selectedAddress = result.data
                            binding.addressTxt.text =
                                selectedAddress?.addressTitle ?: "Unknown Location"
                        }

                        is Resource.Failure -> {
                            binding.locationProgress.visibility = View.GONE
//                            result.message?.let { showLongToast(it) }
                        }

                        is Resource.Loading -> {
                            binding.locationProgress.visibility = View.VISIBLE
                        }

                        else -> {}
                    }
                }
            }
        }
    }
    private fun fragOnClick() {
        binding.locationArea.setOnClickListener {
            if (firebaseAuth.currentUser == null) {
                AppDialog().showingRegisterDialog(
                    Constant.COULDNOT_DO_THIS_ACTON,
                    Constant.PLS_LOGIN
                )
                return@setOnClickListener
            }
            findNavController().navigate(R.id.action_cartFragment_to_allAddressesFragment)
        }
    }

    private fun initViews() {
        binding.appbar.title.text = "My Cart"
        binding.appbar.cardImage.visibility = View.GONE

    }

    private fun getAddressFromAllAddressFrag() {
        findNavController().currentBackStackEntry?.savedStateHandle
            ?.getLiveData<AddressModel>(Constant.ADDRESS)?.observe(viewLifecycleOwner) { result ->
                binding.addressTxt.text = result.addressTitle
                selectedAddress = result
            }
    }


    private fun getProductData() {
        if (registerForCart.not())
            return
        if (firebaseAuth.currentUser == null) {
            showEmptyCartViews()
            return
        }
        viewModel.getCartProduct(
            firebaseAuth.currentUser?.uid ?: ""
        )
    }

    private fun showEmptyCartViews() {
        binding.rvCart.visibility = View.GONE
        binding.imgEmptyBox.visibility = View.VISIBLE
        binding.imgEmptyBoxTexture.visibility = View.VISIBLE
        binding.tvEmptyCart.visibility = View.VISIBLE
        binding.addressTxt.visibility = View.GONE
    }

    private fun getProductDataCallback() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.products.collect {
                    when (it) {
                        is Resource.Success -> {
                            appDialog.dismissProgress()
                            if (it.data?.isEmpty() == true) {
                                showEmptyCartViews()
                                return@collect
                            }
                            registerForCart = false
                            initCartRV(it.data!!)
                            getSelectedAddresses()
                        }

                        is Resource.Loading -> {
                            appDialog.showProgressDialog()
                        }

                        is Resource.Failure -> {
                            appDialog.dismissProgress()
                            Toast.makeText(context, it.message, Toast.LENGTH_LONG).show()
                            getSelectedAddresses()
                        }

                        else -> Unit
                    }
                }
            }
        }

    }

    private fun initCartRV(cartList: List<CartProduct>) {
        adapter = CartRVAdapter(cartList.toMutableList(), this)
        binding.rvCart.adapter = adapter
        RecyclerAnimation.animateRecycler(binding.rvCart)
        adapter.notifyDataSetChanged()
        binding.rvCart.scheduleLayoutAnimation()
        val itemTouchHelper = ItemTouchHelper(onSwipe())
        itemTouchHelper.attachToRecyclerView(binding.rvCart)
    }


    @SuppressLint("NewApi")
    private fun onSwipe(): ItemTouchHelper.SimpleCallback {
        val rvSwipe = RvSwipe().onSwipe { viewHolder, i ->
            posToDelete = viewHolder.absoluteAdapterPosition
            showDeleteItemDialog(posToDelete, viewHolder.itemView)
        }
        return rvSwipe
    }

    private fun deleteProductCallback() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.deleteProduct.collect {
                    when (it) {
                        is Resource.Success -> {
                            handleDeleteItem()
                            appDialog.dismissProgress()
                            showShortSnackBar("Successfully deleted")
                        }

                        is Resource.Loading -> {
                            appDialog.showProgressDialog()
                        }

                        is Resource.Failure -> {
                            appDialog.dismissProgress()
                            Toast.makeText(context, it.message, Toast.LENGTH_LONG).show()
                        }

                        else -> Unit
                    }
                }
            }
        }
    }

    private fun changeProductCountCallback() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.changeCartProductCount.collect {
                    when (it) {
                        is Resource.Success -> {
                            if (it.data == PlusAndMinus.PLUS) {
                                handleProductCount(PlusAndMinus.PLUS)
                            } else if (it.data == PlusAndMinus.MINUS) {
                                handleProductCount(PlusAndMinus.MINUS)
                            }
                            appDialog.dismissProgress()
                            showShortSnackBar("Done")
                        }

                        is Resource.Loading -> {
                            appDialog.showProgressDialog()
                        }

                        is Resource.Failure -> {
                            appDialog.dismissProgress()
                            Toast.makeText(context, it.message, Toast.LENGTH_LONG).show()
                        }

                        else -> Unit
                    }
                }
            }
        }

    }

    private fun noInternetCallback() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.noInternet.collect {
                    showShortSnackBar("No internet connection")
                }
            }
        }
    }

    override fun itemOnClick(product: CartProduct, view: View) {
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun plusOnClick(
        product: CartProduct,
        position: Int,
        price: Double,
        increasePrice: Boolean
    ) {
        posToChange = position
        itemPrice = price
        this.isChecked = increasePrice
        viewModel.changeCartProductCount(
            firebaseAuth.currentUser?.uid ?: "",
            product.product.id,
            product.quantity + 1,
            PlusAndMinus.PLUS
        )
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun minusOnClick(
        product: CartProduct,
        position: Int,
        price: Double,
        increasePrice: Boolean
    ) {
        if (product.quantity == 1) {
            showDeleteItemDialog(position)
            return
        }
        posToChange = position
        itemPrice = price
        this.isChecked = increasePrice
        viewModel.changeCartProductCount(
            firebaseAuth.currentUser?.uid ?: "",
            product.product.id,
            product.quantity - 1,
            PlusAndMinus.MINUS
        )
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun showDeleteItemDialog(position: Int, itemView: View? = null) {
        val appDialog = AppDialog()
        appDialog.showDialog(
            "Delete item",
            "Are you sure you want to delete item",
            "Yes",
            "Cancel",
            R.drawable.delete_vector,
            {
                appDialog.dismiss()
                posToDelete = position
                viewModel.deleteProduct(
                    firebaseAuth.currentUser!!.uid,
                    adapter.data[posToDelete].product.id
                )
            },
            {
                appDialog.dismiss()
                itemView?.translationX = 0f
                adapter.notifyItemChanged(position)
            }
        )
    }

    override fun cartCheckBox(totalPrice: Double) {
        setTotalPrice(totalPrice.toString())
    }

    override fun emptyCart() {
        showEmptyCartViews()
    }

    private fun handleProductCount(operation: PlusAndMinus) {
        adapter.handleProductCount(operation)
    }

    private fun handleDeleteItem() {
        adapter.handleDeleteItem(posToDelete)
    }

    private fun setTotalPrice(price: String) {
        binding.tvTotalprice.text = "$price EGP"

    }

}