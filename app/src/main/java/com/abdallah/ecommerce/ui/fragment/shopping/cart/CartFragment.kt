package com.abdallah.ecommerce.ui.fragment.shopping.cart

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import com.abdallah.ecommerce.R
import com.abdallah.ecommerce.data.model.CartProduct
import com.abdallah.ecommerce.data.model.PlusAndMinus
import com.abdallah.ecommerce.databinding.FragmentCartBinding
import com.abdallah.ecommerce.utils.Constant
import com.abdallah.ecommerce.utils.Resource
import com.abdallah.ecommerce.utils.animation.RecyclerAnimation
import com.abdallah.ecommerce.utils.animation.RvSwipe
import com.abdallah.ecommerce.utils.dialogs.AppDialog
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint

class CartFragment : Fragment(), CartRVAdapter.CartOnClick, CartRVAdapterTest.CartOnClickTest {

    private val viewModel by viewModels<CartViewModel>()
    lateinit var binding: FragmentCartBinding
    lateinit var adapter: CartRVAdapter
    lateinit var adapterTest: CartRVAdapterTest
    lateinit var cartList: ArrayList<CartProduct>
    private var totalPrice = 0.0
    private var itemPrice = 0.0
    private var posToDelete: Int = 0
    private var posToChange: Int = 0
    private var isChecked = false
    private var registerForCart = true
    private var registerdeleteProduct = true
    private var registerProductCount = true
    private val appDialog by lazy { AppDialog() }


    @Inject
    lateinit var firebaseAuth: FirebaseAuth


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCartBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        getProductDataCallback()
        deleteProductCallback()
        changeProductCount()
        getProductData()
        noInternetCallback()
        fragOnClick()
    }

    private fun fragOnClick() {
        binding.locationArea.setOnClickListener {
            if (firebaseAuth.currentUser == null) {
                AppDialog().showingRegisterDialogIfNotRegister(
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

    private fun getProductData() {
        if(registerForCart.not()){
            return
        }
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
    }

    private fun getProductDataCallback() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {

//                if (cartList.isNotEmpty()) {
//                    initCartRV(cartList)
//                    return@repeatOnLifecycle
//                }
                viewModel.products.collect {
                    when (it) {
                        is Resource.Success -> {
                            appDialog.dismissProgress()
                            if (it.data?.isEmpty() == true) {
                                showEmptyCartViews()
                                return@collect
                            }
//                            cartList = it.data!!
//                            initCartRV(cartList)
                            registerForCart = false
                            cartList = (it.data as ArrayList<CartProduct>?)!!
                            val copylist = cartList.map { it.copy() }
                            initCartRVTest(copylist)
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

    private fun initCartRV(cartList: List<CartProduct>) {
        adapter = CartRVAdapter(cartList.toMutableList(), this)
        binding.rvCart.adapter = adapter
        adapter.totalPrice = this.totalPrice
        RecyclerAnimation.animateRecycler(binding.rvCart)
        adapter.notifyDataSetChanged()
        binding.rvCart.scheduleLayoutAnimation()
        val itemTouchHelper = ItemTouchHelper(onSwipe())
        itemTouchHelper.attachToRecyclerView(binding.rvCart)
    }

    private fun initCartRVTest(cartList: List<CartProduct>) {
        // copy the list
//        var copyList = ArrayList<CartProduct>(cartList)
        adapterTest = CartRVAdapterTest(cartList.toMutableList(), this)
        Log.d(
            "test",
            "List hash code the same ? " + (this.cartList.hashCode() == cartList.hashCode())
        )
        binding.rvCart.adapter = adapterTest
        adapterTest.totalPrice = this.totalPrice
        RecyclerAnimation.animateRecycler(binding.rvCart)
        adapterTest.notifyDataSetChanged()
        binding.rvCart.scheduleLayoutAnimation()
        val itemTouchHelper = ItemTouchHelper(onSwipe())
        itemTouchHelper.attachToRecyclerView(binding.rvCart)
    }


    @SuppressLint("NewApi")
    private fun onSwipe(): ItemTouchHelper.SimpleCallback {
        val rvSwipe = RvSwipe().onSwipe { viewHolder, i ->
            posToDelete = viewHolder.layoutPosition
            showDeleteItemDialog(posToDelete, viewHolder.itemView)
        }
        return rvSwipe
    }

    private fun deleteProductCallback() {
        if (registerdeleteProduct.not())
            return
        registerdeleteProduct = false
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.deleteProduct.collect {
                    when (it) {
                        is Resource.Success -> {
                            handleDeleteItem()
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
    private fun changeProductCount() {
        if (registerProductCount.not())
            return
        registerProductCount = false
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.changeCartProductCount.collect {
                    when (it) {
                        is Resource.Success -> {
                            appDialog.dismissProgress()
                            if (it.data == PlusAndMinus.PLUS) {
                                handleProductCountCallback(PlusAndMinus.PLUS)
                            } else if (it.data == PlusAndMinus.MINUS) {
                                handleProductCountCallback(PlusAndMinus.MINUS)
                            }
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
                    Snackbar.make(
                        binding.rvCart,
                        "No internet connection",
                        Snackbar.LENGTH_SHORT
                    )
                        .show()
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
    private fun showDeleteItemDialog(position: Int , itemView : View? = null) {
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
                    adapterTest.list[posToDelete].product.id // crash here
                )
            },
            {
                appDialog.dismiss()
                itemView?.translationX = 0f
//                adapter.notifyItemChanged(position)
                adapterTest.notifyItemChanged(position)
            }
        )
    }
    override fun cartCheckBox(totalPrice: Double) {
        this.totalPrice = totalPrice
        binding.tvTotalprice.text = "$totalPrice EGP"
    }

    private fun handleProductCountCallback(operation: PlusAndMinus) {
        if (isChecked) {
            if (operation == PlusAndMinus.PLUS) adapterTest.totalPrice += itemPrice else adapterTest.totalPrice -= itemPrice
            binding.tvTotalprice.text = "${adapterTest.totalPrice} EGP"
        }

        if (operation == PlusAndMinus.PLUS) cartList[posToChange].quantity += 1
        else cartList[posToChange].quantity -= 1

        cartList[posToChange].isChecked = isChecked
        adapterTest.list

        // this code cartList.map { it.copy()}  for take a copy from list with another allocation
        // to be a differ between adapter list and this class list
        // to make the adapter detect the different between the new list and old one

        adapterTest.list = cartList.toMutableList()
        adapterTest.notifyDataSetChanged()
        Snackbar.make(
            binding.rvCart,
            "Successfully ${operation.toString().lowercase()}",
            Snackbar.LENGTH_SHORT
        ).show()
    }
    private fun handleDeleteItem() {
        val item = adapterTest.list[posToDelete]
        cartList.removeAt(posToDelete)
        if (item.isChecked) {
            val newPrice = item.product.price!! - item.product.offerValue!!
            adapterTest.totalPrice -= (newPrice * item.quantity)
            binding.tvTotalprice.text = adapterTest.totalPrice.toString() + " EGP"
        }
        adapterTest.setAdapterList(cartList.map { it.copy() })
        appDialog.dismissProgress()
        Snackbar.make(
            binding.rvCart,
            "Successfully deleted",
            Snackbar.LENGTH_SHORT
        ).show()
        if (adapterTest.list.isEmpty())
            showEmptyCartViews()
    }

    override fun itemOnClickTest(product: CartProduct, view: View) {
        TODO("Not yet implemented")
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun plusOnClickTest(
        product: CartProduct,
        position: Int,
        price: Double,
        isChecked: Boolean
    ) {
        posToChange = position
        itemPrice = price
        this.isChecked = isChecked
        viewModel.changeCartProductCount(
            firebaseAuth.currentUser?.uid ?: "",
            product.product.id,
            product.quantity + 1,
            PlusAndMinus.PLUS
        )
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun minusOnClickTest(
        product: CartProduct,
        position: Int,
        price: Double,
        isChecked: Boolean
    ) {
        if (product.quantity == 1) {
            showDeleteItemDialog(position)
            return
        }
        posToChange = position
        itemPrice = price
        this.isChecked = isChecked
        viewModel.changeCartProductCount(
            firebaseAuth.currentUser?.uid ?: "",
            product.product.id,
            product.quantity - 1,
            PlusAndMinus.MINUS
        )
    }

    override fun cartCheckBoxTest(totalPrice: Double) {
        this.totalPrice = totalPrice
        binding.tvTotalprice.text = "$totalPrice EGP"
    }


}