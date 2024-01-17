package com.abdallah.ecommerce.ui.fragment.shopping.search

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.abdallah.ecommerce.data.model.Product
import com.abdallah.ecommerce.databinding.FragmentSearchBinding
import com.abdallah.ecommerce.ui.fragment.shopping.allProducts.adapter.AllProductsAdapter
import com.abdallah.ecommerce.utils.Resource
import com.abdallah.ecommerce.utils.animation.RecyclerAnimation
import com.abdallah.ecommerce.utils.animation.ViewAnimation
import com.abdallah.ecommerce.utils.dialogs.AppDialog
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class SearchFragment : Fragment() ,  AllProductsAdapter.AllProductsOnClick{
    @Inject
    lateinit var firebaseAuth: FirebaseAuth
    private val viewModel by viewModels<SearchViewModel>()
    private val RECENT_SEARCH = "Recent Search"
    private val PRODUCTS = "Products"
    private var registerCallback = true
    private lateinit var binding: FragmentSearchBinding
    private val appDialog by lazy { AppDialog() }




    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchBinding.inflate(inflater)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showKeyboardAutomatically()
        initViews()
        searchCallBack()
        noInternetCallBack()
        fragOnClick()
    }
    private fun initViews() {
        binding.toolbar.title.text = "Search"
        binding.recyclerTitle.text = "Recent Search"
        binding.toolbar.cardImage.visibility = View.GONE
    }
    private fun changeRecyclerTitle(label : String) {
        ViewAnimation().viewAnimation(binding.recyclerTitle ,binding.parentArea)
        binding.recyclerTitle.text = label
    }
    private fun showFailImgWithLabel(label : String) {
        ViewAnimation().viewAnimation(binding.noProductsImg ,binding.parentArea)
        ViewAnimation().viewAnimation(binding.noProductsTxt ,binding.parentArea)
        binding.noProductsTxt.text = label
        binding.noProductsTxt.visibility = View.VISIBLE
        binding.noProductsImg.visibility = View.VISIBLE
    }
    private fun hideFailImgWithLabel() {
        binding.noProductsTxt.visibility = View.GONE
        binding.noProductsImg.visibility = View.GONE
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun search(searchText: String) {
        viewModel.getProductFromSearch(searchText)
    }
    private fun noInternetCallBack() {
        lifecycleScope.launchWhenStarted {
            viewModel.noInternet.collect {
                Snackbar.make(binding.searchEd , "No Internet connection", Snackbar.LENGTH_SHORT).show()
            }
        }

    }
    private fun searchCallBack() {
        if (registerCallback.not())
            return
        registerCallback = false
        lifecycleScope.launchWhenStarted {
            viewModel.searchProducts.collect { result ->
                when (result) {
                    is Resource.Success -> {
                        appDialog.dismissProgress()
                        hideFailImgWithLabel()
                        if (result.data!!.isEmpty()) {
                            binding.recyclerView.visibility = View.GONE
                            showFailImgWithLabel("No products founded")

                            return@collect
                        }
                        binding.recyclerView.visibility = View.VISIBLE
                        changeRecyclerTitle(PRODUCTS)
                        initProductsRv(result.data)
                    }
                    is Resource.Failure -> {
                        appDialog.dismissProgress()
                        Toast.makeText(
                            context, "Error occured " + result.message,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    is Resource.Loading -> {
                        appDialog.showProgressDialog()
                    }
                    else -> {}
                }
            }
        }
    }
    private fun initProductsRv(data: ArrayList<Product>) {
        val allProductsAdapter = AllProductsAdapter(data , this)
        binding.recyclerView.adapter = allProductsAdapter
        RecyclerAnimation.animateRecycler(binding.recyclerView)
        allProductsAdapter.notifyDataSetChanged()
        binding.recyclerView.scheduleLayoutAnimation()
    }
    private fun showKeyboardAutomatically() {
        val inputMethodManger =
            activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManger.toggleSoftInput(
            InputMethodManager.SHOW_FORCED,
            InputMethodManager.HIDE_IMPLICIT_ONLY
        )

        binding.searchEd.requestFocus()
    }
    override fun itemOnClick(product: Product, view: View) {
    }

    override fun cartOnClick(productId: String, product: Product) {
    }
    var job : Job = Job()
    var coroutineScope = CoroutineScope(Dispatchers.IO)
    @RequiresApi(Build.VERSION_CODES.M)
    private fun fragOnClick(){
        binding.apply {
            searchEd.setOnEditorActionListener { textView, i, keyEvent ->
                if (i === EditorInfo.IME_ACTION_SEARCH) {
                    if (textView.text.toString().isNotEmpty())
                        search(textView.text.toString())
                    return@setOnEditorActionListener true
                }
                false
            }
            searchEd.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                @RequiresApi(Build.VERSION_CODES.M)
                override fun afterTextChanged(p0: Editable?) {
                    job.cancel()
                    job = coroutineScope.launch {
                        delay(700)

                    }
                }
            })
        }
    }

}