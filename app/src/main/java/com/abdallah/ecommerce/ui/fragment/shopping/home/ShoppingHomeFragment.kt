package com.abdallah.ecommerce.ui.fragment.shopping.home

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import com.abdallah.ecommerce.R
import com.abdallah.ecommerce.application.core.BaseFragment
import com.abdallah.ecommerce.data.model.Categories
import com.abdallah.ecommerce.data.model.Category
import com.abdallah.ecommerce.data.model.Product
import com.abdallah.ecommerce.databinding.FragmentShoppingHomeBinding
import com.abdallah.ecommerce.ui.fragment.shopping.home.adapter.BannerRecAdapter
import com.abdallah.ecommerce.ui.fragment.shopping.home.adapter.BestDealsAdapter
import com.abdallah.ecommerce.ui.fragment.shopping.home.adapter.MainCategoryAdapter
import com.abdallah.ecommerce.utils.Constant
import com.abdallah.ecommerce.utils.Constant.PRODUCT_TRANSITION_NAME
import com.abdallah.ecommerce.utils.Resource
import com.abdallah.ecommerce.utils.animation.RecyclerAnimation
import com.abdallah.ecommerce.utils.dialogs.AppDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class ShoppingHomeFragment : BaseFragment<FragmentShoppingHomeBinding>(),
    BestDealsAdapter.BestDealsTestOnClick, MainCategoryAdapter.MainCategoryOnClick {

    private val viewModel by viewModels<ShoppingHomeViewModel>()
    private var registerForBanner = true
    private var registerForCategories = true
    private var registerForProducts = true
    private var bannerCurrentPosition = 0
    private var categoryList: ArrayList<Category> = ArrayList()

    @Inject
    lateinit var firestore: FirebaseFirestore

    @Inject
    lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("test" , " onCreate ShoppingHomeFragment")
    }
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        noInternetCallBack()
        downloadBannerImages()
        getCategories()
        getProducts()
        downloadBannerImagesCallBack()
        getCategoriesCallBack()
        getProductsCallBack()
        fragOnClick()
        addProductToCartCallback()
        swipeRefreshCallback()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun swipeRefreshCallback() {
        binding.swiperefresh.setOnRefreshListener{
            viewModel.downloadBannerImages()
            viewModel.getCategories()
            viewModel.getOfferedProducts()
            binding.swiperefresh.isRefreshing = false
        }
    }

    private fun fragOnClick() {
        binding.seeMoreCategories.setOnClickListener {
            if (categoryList.isEmpty()) {
                Toast.makeText(requireContext(), "couldn't see more now...", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }
            val categories = Categories()
            categories.addAll(categoryList)
            val action = ShoppingHomeFragmentDirections.actionHomeFragmentToAllProducts(
                categories,
                categoryList[0].categoryName!!,
                0
            )
            findNavController().navigate(action)
        }
        binding.searchArea.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_searchFragment)
        }
        binding.micArae.setOnClickListener {
            binding.searchArea.performClick()
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun downloadBannerImages() {
        if (registerForBanner.not()) {
            return
        }
        viewModel.downloadBannerImages()
    }
    private fun downloadBannerImagesCallBack() {
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.imageList.collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            stopBannerShimmer()
                            registerForBanner = false
                            result.data?.let {
                                createBanner(it)
                                autoLoopBanner()
                            }
                        }
                        is Resource.Failure -> {
                            stopBannerShimmer()
                            val localImgList =
                                arrayListOf(R.drawable.home_banner_1, R.drawable.home_banner_2)
                            createBanner(null, localImgList)
                        }

                        is Resource.Loading -> {
                            startBannerShimmer()
                        }

                        else -> {}
                    }
                }
            }
        }
    }


    override fun onPause() {
        super.onPause()
        job.cancel()
    }

    override fun onResume() {
        super.onResume()
        autoLoopBanner()
    }

    var coroutine: CoroutineScope = CoroutineScope(Dispatchers.IO)
    var job: Job = Job()


    private fun autoLoopBanner() {
        job.cancel()
        job = coroutine.launch() {
            while (true) {
                delay(5000L)
                val bannerLastItem = binding.bannerHomeParent.adapter?.itemCount ?: return@launch
                if (bannerCurrentPosition == bannerLastItem) {
                    bannerCurrentPosition = 0
                }
                binding.bannerHomeParent.smoothScrollToPosition(bannerCurrentPosition)
                bannerCurrentPosition++
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun getCategories() {
        if(registerForCategories.not()){
            return
        }
        viewModel.getCategories()
    }
    @RequiresApi(Build.VERSION_CODES.M)
    private fun getCategoriesCallBack() {
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.categoryList.collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            stopMainCategoryShimmer()
                            result.data?.let {
                                registerForCategories = false
                                initMainCategoryRv(it)
                                categoryList = it
                            }
                        }
                        is Resource.Failure -> {
                            stopMainCategoryShimmer()
                            Toast.makeText(
                                context, "Error occured " + result.message,
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        is Resource.Loading -> {
                            startMainCategoryShimmer()
                        }

                        else -> {}
                    }
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun getProducts() {
        if(registerForProducts.not()){
            return
        }
        viewModel.getOfferedProducts()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun getProductsCallBack() {

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.offeredProducts.collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            stopDealsShimmer()
                            result.data?.let {
                                registerForProducts = false
                                initOfferedRv(it)
                            }
                        }
                        is Resource.Failure -> {
                            stopDealsShimmer()
                            Toast.makeText(
                                context, "Error occured " + result.message,
                                Toast.LENGTH_SHORT
                            ).show()
                            Log.d("test", "Firebase Err ${result.message}")
                        }

                        is Resource.Loading -> {
                            startDealsShimmer()
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
                    showShortSnackBar("No Internet connection")
                }
            }
        }
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.noInternetAddProduct.collect {
                    showShortSnackBar("No Internet connection")
                }
            }
        }
    }

    private fun startBannerShimmer() {
        binding.shimmerBanner.visibility = View.VISIBLE
        binding.bannerHomeParent.visibility = View.INVISIBLE
        binding.shimmerBanner.startShimmer()
    }

    private fun stopBannerShimmer() {
        binding.shimmerBanner.stopShimmer()
        binding.shimmerBanner.visibility = View.INVISIBLE
        binding.bannerHomeParent.visibility = View.VISIBLE

    }

    private fun startMainCategoryShimmer() {
        binding.shimmerMainCategory.visibility = View.VISIBLE
        binding.shimmerMainCategory.startShimmer()
        binding.categoriesArea.visibility = View.INVISIBLE
        binding.mainRecCategory.visibility = View.INVISIBLE
        binding.shimmerCategoriesArea.visibility = View.VISIBLE
        binding.shimmerCategoriesArea.startShimmer()
    }

    private fun stopMainCategoryShimmer() {
        binding.shimmerMainCategory.stopShimmer()
        binding.shimmerMainCategory.visibility = View.INVISIBLE
        binding.categoriesArea.visibility = View.VISIBLE
        binding.shimmerCategoriesArea.stopShimmer()
        binding.shimmerCategoriesArea.visibility = View.INVISIBLE
        binding.mainRecCategory.visibility = View.VISIBLE

    }

    private fun startDealsShimmer() {
        binding.shimmerDealsArea.startShimmer()
        binding.shimmerDealsTxt.startShimmer()
        binding.shimmerDealsArea.visibility = View.VISIBLE
        binding.shimmerDealsTxt.visibility = View.VISIBLE
        binding.bestDealsContainer.visibility = View.INVISIBLE
        binding.bestDealsRV.visibility = View.INVISIBLE
    }

    private fun stopDealsShimmer() {
        binding.shimmerDealsArea.stopShimmer()
        binding.shimmerDealsTxt.stopShimmer()
        binding.shimmerDealsArea.visibility = View.INVISIBLE
        binding.shimmerDealsTxt.visibility = View.INVISIBLE
        binding.bestDealsContainer.visibility = View.VISIBLE
        binding.bestDealsRV.visibility = View.VISIBLE


    }

    private fun initMainCategoryRv(data: ArrayList<Category>) {
        val adapter = MainCategoryAdapter(data, this)
        binding.mainRecCategory.adapter = adapter
        RecyclerAnimation.animateRecycler(binding.mainRecCategory)
        adapter.notifyDataSetChanged()
        binding.bannerHomeParent.scheduleLayoutAnimation()
    }

    @SuppressLint("SuspiciousIndentation")
    private fun initOfferedRv(data: ArrayList<Product>) {
        val bestDealsAdapter = BestDealsAdapter()
        bestDealsAdapter.differ.submitList(data)
        bestDealsAdapter.listener = this
        binding.bestDealsRV.adapter = bestDealsAdapter
        RecyclerAnimation.animateRecycler(binding.bestDealsRV)
        binding.bannerHomeParent.scheduleLayoutAnimation()
    }

    private fun createBanner(uriList: ArrayList<Uri>? = null, localImages: ArrayList<Int>? = null) {
        var bannerAdapter: BannerRecAdapter =
            if (uriList != null)
                BannerRecAdapter(uriList)
            else
                BannerRecAdapter(null, localImages)
        binding.bannerHomeParent.adapter = bannerAdapter
        RecyclerAnimation.animateRecycler(binding.bannerHomeParent)
        bannerAdapter.notifyDataSetChanged()
        binding.bannerHomeParent.scheduleLayoutAnimation()
    }

    override fun mainCategoryOnClick(position: Int, categoryName: String) {
        val categories = Categories()
        categories.addAll(categoryList)
        val action = ShoppingHomeFragmentDirections.actionHomeFragmentToAllProducts(
            categories,
            categoryName,
            position
        )
        findNavController().navigate(action)
    }

    override fun itemOnClick(product: Product, img: View) {
        val extras = FragmentNavigatorExtras(
            img to PRODUCT_TRANSITION_NAME
        )
        img.transitionName = PRODUCT_TRANSITION_NAME
        val action =
            ShoppingHomeFragmentDirections.actionHomeFragmentToProductDetailsFragment(product)
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

    @RequiresApi(Build.VERSION_CODES.M)
    private fun addProductToCart(product: Product) {
        viewModel.addProductToCart(
            firebaseAuth.currentUser?.uid ?: "",
            product,
            -1,
            ""
        )
    }

    private fun addProductToCartCallback() {
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.addToCartFlow.collect {
                    when (it) {
                        is Resource.Success -> {
                            hideProgressDialog()
                            Toast.makeText(
                                context,
                                "Product added successfully",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }

                        is Resource.Loading -> {
                            showProgressDialog()
                        }

                        is Resource.Failure -> {
                            hideProgressDialog()
                            Toast.makeText(context, it.message, Toast.LENGTH_LONG).show()
                        }

                        else -> {}
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("test", "onDestroyView shopping Frag")
    }

}

