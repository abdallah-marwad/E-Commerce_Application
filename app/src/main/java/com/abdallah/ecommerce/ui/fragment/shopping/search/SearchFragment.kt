package com.abdallah.ecommerce.ui.fragment.shopping.search

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.speech.RecognizerIntent
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import com.abdallah.ecommerce.application.core.BaseFragment
import com.abdallah.ecommerce.data.model.Product
import com.abdallah.ecommerce.data.sharedPreferences.SharedPreferencesHelper
import com.abdallah.ecommerce.databinding.FragmentSearchBinding
import com.abdallah.ecommerce.ui.fragment.shopping.allProducts.adapter.AllProductsAdapter
import com.abdallah.ecommerce.utils.Constant
import com.abdallah.ecommerce.utils.Resource
import com.abdallah.ecommerce.utils.animation.RecyclerAnimation
import com.abdallah.ecommerce.utils.animation.RvSwipe
import com.abdallah.ecommerce.utils.animation.ViewAnimation
import com.abdallah.ecommerce.utils.dialogs.AppDialog
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import java.util.Objects
import javax.inject.Inject


@AndroidEntryPoint
class SearchFragment : BaseFragment<FragmentSearchBinding>(), AllProductsAdapter.AllProductsOnClick,
    RecentSearchAdapter.HistoryOnClick {
    @Inject
    lateinit var firebaseAuth: FirebaseAuth
    private val viewModel by viewModels<SearchViewModel>()
    private val RECENT_SEARCH = "Recent Search"
    private val PRODUCTS = "Products"
    private val REQUEST_CODE_SPEECH_INPUT = 10
    private var edCallback: TextWatcher? = null
    private var enableSearchHistory = true
    private var recentSearch: MutableSet<String>? = null


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        searchCallBack()
        noInternetCallBack()
        initEdCallback()
        fragOnClick()
        addProductToCartCallback()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onStart() {
        super.onStart()
        if (binding.searchEd.text.toString().isNotEmpty()) {
            return
        }
        getRecentSearch()?.let { ArrayList(it) }
    }

    override fun onPause() {
        super.onPause()
        enableSearchHistory = false
    }

    private fun initViews() {
        binding.toolbar.title.text = "Search"
        binding.recyclerTitle.text = "Recent Search"
        binding.toolbar.cardImage.visibility = View.GONE
    }

    private fun changeRecyclerTitle(label: String) {
        ViewAnimation().viewAnimation(binding.recyclerTitle, binding.parentArea)
        binding.recyclerTitle.text = label
    }

    private fun showFailImgWithLabel(label: String) {
        ViewAnimation().viewAnimation(binding.noProductsImg, binding.parentArea)
        ViewAnimation().viewAnimation(binding.noProductsTxt, binding.parentArea)
        binding.noProductsTxt.text = label
        binding.noProductsTxt.visibility = View.VISIBLE
        binding.noProductsImg.visibility = View.VISIBLE
        binding.recyclerViewProducts.visibility = View.INVISIBLE
        binding.recyclerViewHistory.visibility = View.INVISIBLE

    }

    private fun hideFailImgWithLabel() {
        binding.noProductsTxt.visibility = View.GONE
        binding.noProductsImg.visibility = View.GONE
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun getAllProducts() {
        if (viewModel.products == null)
            viewModel.getProductFromSearch()


    }

    private fun getRecentSearch(): MutableSet<String>? {
        var recentSearch: MutableSet<String>?
        val gson = Gson()
        recentSearch = gson.fromJson(
            SharedPreferencesHelper.getString(RECENT_SEARCH),
            object : TypeToken<MutableSet<String>?>() {}.type
        )
        this.recentSearch = recentSearch
        return recentSearch
    }


    private fun saveSearchWord(searchTxt: String) {
        if (recentSearch == null)
            recentSearch = getRecentSearch()
        if (recentSearch == null)
            recentSearch = HashSet()

        recentSearch?.add(searchTxt)
        val toJson = Gson().toJson(recentSearch)
        SharedPreferencesHelper.addString(RECENT_SEARCH, toJson)
        return
    }

    private fun noHistroySearchImpl() {
        binding.recyclerViewProducts.visibility = View.GONE
        binding.recyclerViewHistory.visibility = View.GONE
        showFailImgWithLabel("Not Found History Search")
        binding.recyclerTitle.text = RECENT_SEARCH
    }

    private fun initHistoryRv(data: ArrayList<String>) {
        if (data == null) {
            noHistroySearchImpl()
            return
        }
        binding.recyclerViewHistory.visibility = View.VISIBLE
        binding.recyclerViewProducts.visibility = View.GONE
        binding.recyclerTitle.text = RECENT_SEARCH


        hideFailImgWithLabel()
        val adapter = RecentSearchAdapter(data, this)
        binding.recyclerViewHistory.adapter = adapter
        val itemTouchHelper = ItemTouchHelper(onSwipe(adapter))
        itemTouchHelper.attachToRecyclerView(binding.recyclerViewHistory)
        adapter.notifyDataSetChanged()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun searchCallBack() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.searchProducts.collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            binding.progressBar.visibility = View.GONE
                            viewModel.products = result.data
                            if (binding.searchEd.text.toString().isNotEmpty()) {
                                hideKeyboardAutomatically()
                                searchProductsWithFilter(binding.searchEd.text.toString())
                                return@collect
                            }
                        }

                        is Resource.Failure -> {
                            binding.progressBar.visibility = View.GONE
                            Toast.makeText(
                                context, "Error occured " + result.message,
                                Toast.LENGTH_LONG
                            ).show()
                        }

                        is Resource.Loading -> {
                            binding.progressBar.visibility = View.VISIBLE
                            binding.recyclerViewHistory.visibility = View.GONE
                            binding.recyclerViewProducts.visibility = View.GONE

                        }

                        else -> {}
                    }
                }
            }
        }
    }

    private fun initProductsRv(data: ArrayList<Product>) {
        if (data.isEmpty()) {
            binding.recyclerViewProducts.visibility = View.GONE
            binding.recyclerViewHistory.visibility = View.GONE
            showFailImgWithLabel("No products founded")
            binding.recyclerTitle.text = PRODUCTS
            return
        } else {
            binding.recyclerViewProducts.visibility = View.VISIBLE
            binding.recyclerViewHistory.visibility = View.GONE
            binding.recyclerTitle.text = PRODUCTS

        }
        hideFailImgWithLabel()
        val allProductsAdapter = AllProductsAdapter(data, this)
        binding.recyclerViewProducts.adapter = allProductsAdapter
        RecyclerAnimation.animateRecycler(binding.recyclerViewProducts)
        allProductsAdapter.notifyDataSetChanged()
        binding.recyclerViewProducts.scheduleLayoutAnimation()
    }

    private fun hideKeyboardAutomatically() {
        binding.searchEd.clearFocus()
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        var view = view?.rootView?.windowToken
        imm?.hideSoftInputFromWindow(view, 0)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun searchProductsWithFilter(searchTxt: String) {
        if (viewModel.products == null) {
            getAllProducts()
            return
        }
        if (viewModel.products!!.isEmpty()) {
            getAllProducts()
            return
        }
        val searchProducts =
            viewModel.products!!.filter {
                it.productName!!.lowercase().contains(searchTxt.lowercase())
            } as ArrayList<Product>

        if (searchProducts.isEmpty()) {
            showFailImgWithLabel("No products found")
        }
        initProductsRv(searchProducts)
    }

    private fun searchHistoryWithFilter(searchTxt: String) {
        if (recentSearch == null) {
            recentSearch = Gson().fromJson(
                SharedPreferencesHelper.getString(RECENT_SEARCH),
                object : TypeToken<MutableSet<String>?>() {}.type
            )
        }
        if (recentSearch == null) {
            showFailImgWithLabel("No recent search found")
            return
        }
        val searchHistory = ArrayList(recentSearch)
        val filteredList = searchHistory.filter {
            it.lowercase().contains(searchTxt.lowercase())
        }
        if (filteredList.isEmpty()) {
            showFailImgWithLabel("No recent search found")
            return
        }
        /*
        * Should change the data set not recreate new adapter
        * */
        initHistoryRv(ArrayList(filteredList))

    }

    override fun itemOnClick(product: Product, view: View) {
        val extras = FragmentNavigatorExtras(
            view to Constant.PRODUCT_TRANSITION_NAME
        )
        view.transitionName = Constant.PRODUCT_TRANSITION_NAME
        val action =
            SearchFragmentDirections.actionSearchFragmentToProductDetailsFragment(product)
        findNavController().navigate(action, extras)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun cartOnClick(productId: String, product: Product) {
        if (firebaseAuth.currentUser == null) {
            AppDialog().showingRegisterDialog(
                Constant.COULDNOT_DO_THIS_ACTON,
                Constant.PLS_LOGIN
            )
            return
        }
        addProductToCart(product)
    }

    var job: Job = Job()
    var coroutineScope = CoroutineScope(Dispatchers.IO)

    private fun initEdCallback() {
        edCallback = object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            @RequiresApi(Build.VERSION_CODES.M)
            override fun afterTextChanged(p0: Editable?) {
                if (enableSearchHistory.not()) {
                    enableSearchHistory = true
                    return
                }
                job.cancel()
                job = coroutineScope.launch {
                    withContext(Dispatchers.Main) {
                        searchHistoryWithFilter(p0.toString())
                    }
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun fragOnClick() {
        binding.apply {
            searchEd.setOnEditorActionListener { textView, i, keyEvent ->
                if (i === EditorInfo.IME_ACTION_SEARCH) {
                    if (textView.text.toString().isNotEmpty()) {
                        hideKeyboardAutomatically()
                        searchProductsWithFilter(textView.text.toString())
                        saveSearchWord(textView.text.toString())
                        return@setOnEditorActionListener true
                    }
                }
                false
            }
            searchEd.addTextChangedListener(edCallback)
            imgMic.setOnClickListener {
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                intent.putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                )
                intent.putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE,
                    Locale.getDefault()
                )
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to text")
                try {
                    startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT)
                } catch (e: Exception) {
                    Toast
                        .makeText(
                            context, "Some Error happen" + e.message,
                            Toast.LENGTH_SHORT
                        )
                        .show()
                }
            }
        }
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

    override fun onStop() {
        super.onStop()
        hideKeyboardAutomatically()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun itemOnClick(searchTxt: String) {
        enableSearchHistory = false
        binding.searchEd.setText(searchTxt)
        searchProductsWithFilter(searchTxt)

    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun copyOnClick(searchTxt: String) {
        binding.searchEd.setText(searchTxt)
        showKeyboardAutomatically()
    }

    override fun onActivityResult(
        requestCode: Int, resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SPEECH_INPUT) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                val result = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS
                )
                binding.searchEd.setText(
                    Objects.requireNonNull(result)?.get(0)
                )
                showKeyboardAutomatically()
            }
        }
    }

    private fun addProductToCartCallback() {
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                viewModel.addToCartFlow.collect {
                    when (it) {
                        is Resource.Success -> {
                            hideProgressDialog()
                            showShortToast("Product added successfully")
                        }

                        is Resource.Loading -> {
                            showProgressDialog()
                        }

                        is Resource.Failure -> {
                            hideProgressDialog()
                            showShortToast(it.message ?: "Failed to add it")
                        }

                        else -> {}
                    }
                }
            }
        }
    }

    private fun noInternetCallBack() {
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.noInternet.collect {
                    Snackbar.make(binding.searchEd, "No Internet connection", Snackbar.LENGTH_SHORT)
                        .show()
                }
            }
            lifecycleScope.launch {
                viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    viewModel.noInternetCart.collect {
                        Snackbar.make(
                            binding.searchEd,
                            "No Internet connection",
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

    }

    private fun onSwipe(adapter: RecentSearchAdapter): ItemTouchHelper.SimpleCallback {
        val rvSwipe = RvSwipe().onSwipe { viewHolder, i ->
            val customViewHolder = viewHolder as RecentSearchAdapter.ViewHolder
            val txt = customViewHolder.binding.historyTxt.text.toString()
            recentSearch!!.remove(txt)
            adapter.data.remove(txt)
            adapter.notifyDataSetChanged()
            if (adapter.data.isEmpty())
                showFailImgWithLabel("No recent search found")
            val toJson = Gson().toJson(recentSearch)
            SharedPreferencesHelper.addString(RECENT_SEARCH, toJson)
        }
        return rvSwipe
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun addProductToCart(product: Product) {
        viewModel.addProductToCart(
            firebaseAuth.currentUser?.uid ?: "",
            product,
            -1,
            ""
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.searchEd.removeTextChangedListener(edCallback)
    }
}