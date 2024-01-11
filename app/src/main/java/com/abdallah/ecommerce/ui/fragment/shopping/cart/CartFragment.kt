package com.abdallah.ecommerce.ui.fragment.shopping.cart

import android.R
import android.graphics.Canvas
import android.os.Build
import android.os.Bundle
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
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.abdallah.ecommerce.data.model.CartProduct
import com.abdallah.ecommerce.databinding.FragmentCartBinding
import com.abdallah.ecommerce.utils.Resource
import com.abdallah.ecommerce.utils.animation.RecyclerAnimation
import com.abdallah.ecommerce.utils.dialogs.AppDialog
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint

class CartFragment : Fragment(), CartRVAdapter.CartOnClick {

    private val viewModel by viewModels<CartViewModel>()
    lateinit var binding: FragmentCartBinding
    lateinit var adapter: CartRVAdapter
    private var posToDelete: Int= 0
    private var registerCallback = true
    private var registerdeleteProduct = true
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
        getProductDataCallback()
        deleteProductCallback()
        getProductData()
    }

    private fun getProductData() {
        viewModel.getCartProduct(
            firebaseAuth.currentUser?.uid ?: ""
        )
    }

    private fun getProductDataCallback() {
        if (registerCallback.not())
            return
        registerCallback = false
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.products.collect {
                    when (it) {
                        is Resource.Success -> {
                            appDialog.dismissProgress()
                            initCartRV(it.data!!)
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
                val productID = adapter.data.get(posToDelete)

                viewModel.deleteProduct(
                    firebaseAuth.currentUser!!.uid,
                    productID.product.id
                )
            }
//
//            fun onChildDraw(
//                c: Canvas?,
//                recyclerView: RecyclerView?,
//                viewHolder: RecyclerView.ViewHolder?,
//                dX: Float,
//                dY: Float,
//                actionState: Int,
//                isCurrentlyActive: Boolean
//            ) {
//                RecyclerView.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
//                    .addActionIcon(R.drawable.)
//                    .create()
//                    .decorate()
//                super.onChildDraw(
//                    c!!, recyclerView!!,
//                    viewHolder!!, dX, dY, actionState, isCurrentlyActive
//                )
//            }

        }
        return itemTouchHelper
    }
    private fun deleteProductCallback() {
        if (registerdeleteProduct.not())
            return
        registerdeleteProduct = false
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.products.collect {
                    when (it) {
                        is Resource.Success -> {
                            appDialog.dismissProgress()
                            Snackbar.make(binding.rvCart, "Successfully deleted", Toast.LENGTH_LONG).show()
                            adapter.data.removeAt(posToDelete)
                            adapter.notifyItemRemoved(posToDelete)
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
    override fun itemOnClick(product: CartProduct, view: View) {

    }

    override fun plusOnClick(product: CartProduct, view: View) {
    }

    override fun minusOnClick(product: CartProduct, view: View) {
    }

}