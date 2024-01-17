package com.abdallah.ecommerce.ui.fragment.shopping.cart

import android.app.ProgressDialog.show
import android.content.Intent
import android.graphics.Canvas
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.abdallah.ecommerce.R
import com.abdallah.ecommerce.application.MyApplication
import com.abdallah.ecommerce.data.model.CartProduct
import com.abdallah.ecommerce.data.model.PlusAndMinus
import com.abdallah.ecommerce.databinding.FragmentCartBinding
import com.abdallah.ecommerce.ui.activity.LoginRegisterActivity
import com.abdallah.ecommerce.utils.Resource
import com.abdallah.ecommerce.utils.animation.RecyclerAnimation
import com.abdallah.ecommerce.utils.dialogs.AppDialog
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint

class CartFragment : Fragment(), CartRVAdapter.CartOnClick {

    private val viewModel by viewModels<CartViewModel>()
    lateinit var binding: FragmentCartBinding
    lateinit var adapter: CartRVAdapter
    private var totalPrice = 0.0
    private var itemPrice = 0.0
    private var posToDelete: Int = 0
    private var posToChange: Int = 0
    private var isChecked = false
    private var registerdeleteProduct = true
    private var registerProductCount = true
    private val appDialog by lazy { AppDialog() }
    private lateinit var selectedProducts: ArrayList<CartProduct>
    private var cartList: List<CartProduct> = ArrayList()

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
    }

    private fun initViews() {
        binding.appbar.title.text = "My Cart"
        binding.appbar.cardImage.visibility = View.GONE
    }

    private fun getProductData() {
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

                if (cartList.isNotEmpty()) {
                    initCartRV(cartList)
                    return@repeatOnLifecycle
                }
                viewModel.products.collect {
                    when (it) {
                        is Resource.Success -> {
                            appDialog.dismissProgress()
                            if (it.data?.isEmpty() == true) {
                                showEmptyCartViews()
                                return@collect
                            }
                            cartList = it.data!!
                            initCartRV(cartList)
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
        val itemTouchHelper = ItemTouchHelper(onSwipe(adapter))
        itemTouchHelper.attachToRecyclerView(binding.rvCart)
    }


    private fun onSwipe(adapter: CartRVAdapter): ItemTouchHelper.SimpleCallback {
        val itemTouchHelper = object :
            ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            @RequiresApi(Build.VERSION_CODES.M)
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                posToDelete = viewHolder.absoluteAdapterPosition
                showDeleteItemDialog(posToDelete , viewHolder.itemView)
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                super.onChildDraw(
                    c,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )
                RecyclerViewSwipeDecorator.Builder(
                    c,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )
                    .addActionIcon(R.drawable.baseline_delete_outline_24)
                    .create()
                    .decorate()
            }

        }
        return itemTouchHelper
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
    private fun handleDeleteItem() {
        val item = adapter.data[posToDelete]
        appDialog.dismissProgress()
        adapter.data.removeAt(posToDelete)
        if(item.isChecked){
            val newPrice = item.product.price!! - item.product.offerValue!!
            adapter.totalPrice -= (newPrice * item.quantity)
            binding.tvTotalprice.text = adapter.totalPrice.toString()+" EGP"
        }
        adapter.notifyDataSetChanged()
        Snackbar.make(
            binding.rvCart,
            "Successfully deleted",
            Snackbar.LENGTH_SHORT
        ).show()
        if (adapter.data.isEmpty())
            showEmptyCartViews()
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
        this.totalPrice = totalPrice
        binding.tvTotalprice.text = "$totalPrice EGP"
    }
    private fun handleProductCountCallback(operation: PlusAndMinus) {
        if (isChecked) {
            if (operation == PlusAndMinus.PLUS) adapter.totalPrice += itemPrice else adapter.totalPrice -= itemPrice
            binding.tvTotalprice.text = "${adapter.totalPrice} EGP"
        }
        if (operation == PlusAndMinus.PLUS) adapter.data[posToChange].quantity += 1
        else adapter.data[posToChange].quantity -= 1
        adapter.data[posToChange].isChecked = isChecked
        adapter.notifyDataSetChanged()
        Snackbar.make(
            binding.rvCart,
            "Successfully ${operation.toString().lowercase()}",
            Snackbar.LENGTH_SHORT
        ).show()
    }


}