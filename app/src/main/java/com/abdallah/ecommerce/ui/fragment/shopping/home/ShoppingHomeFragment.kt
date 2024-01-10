package com.abdallah.ecommerce.ui.fragment.shopping.home

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
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
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import com.abdallah.ecommerce.R
import com.abdallah.ecommerce.data.model.Categories
import com.abdallah.ecommerce.data.model.Category
import com.abdallah.ecommerce.data.model.Product
import com.abdallah.ecommerce.databinding.FragmentShoppingHomeBinding
import com.abdallah.ecommerce.ui.activity.ShoppingActivity
import com.abdallah.ecommerce.ui.fragment.shopping.home.adapter.BannerRecAdapter
import com.abdallah.ecommerce.ui.fragment.shopping.home.adapter.BestDealsAdapter
import com.abdallah.ecommerce.ui.fragment.shopping.home.adapter.MainCategoryAdapter
import com.abdallah.ecommerce.utils.Constant
import com.abdallah.ecommerce.utils.Constant.PRODUCT_TRANSITION_NAME
import com.abdallah.ecommerce.utils.animation.RecyclerAnimation
import com.abdallah.ecommerce.utils.Resource
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
class ShoppingHomeFragment : Fragment(R.layout.fragment_shopping_home),
    BestDealsAdapter.BestDealsOnClick, MainCategoryAdapter.MainCategoryOnClick {

    private lateinit var binding: FragmentShoppingHomeBinding
    private val viewModel by viewModels<ShoppingHomeViewModel>()

    private var bannerCurrentPosition = 0
    private var scrollingRunnable: Runnable = Runnable {}
    private val handler: Handler by lazy { Handler() }
    private val appDialog by lazy { AppDialog() }
    private var categoryList: ArrayList<Category> = ArrayList()
    private var productsList: ArrayList<Product> = ArrayList()
    private var bannersList: ArrayList<Uri> = ArrayList()

    @Inject
    lateinit var firestore: FirebaseFirestore
    @Inject
    lateinit var firebaseAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentShoppingHomeBinding.inflate(inflater)
        val shoppingActivity = activity as ShoppingActivity
        shoppingActivity.showNavBar()
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startBannerShimmer()
        startMainCategoryShimmer()
        startDealsShimmer()
        downloadBannerImages()
        downloadBannerImagesCallBack()
        getCategories()
        getProducts()
        noInternetCallBack()
        fragOnClick()
        addProductToCartCallback()


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
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun downloadBannerImages() {
        if (bannersList.isEmpty().not()) {
            return
        }
        viewModel.downloadBannerImages()
    }

    private fun downloadBannerImagesCallBack() {
        if (bannersList.isEmpty().not()) {
            stopBannerShimmer()
            createBanner(bannersList)
            autoLoopBanner()
            return
        }
        lifecycleScope.launchWhenResumed {
            viewModel.imageList.collect { result ->
                when (result) {
                    is Resource.Success -> {
                        stopBannerShimmer()
                        result.data?.let {
                            bannersList.clear()
                            bannersList.addAll(it)
                            createBanner(bannersList)
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



    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(scrollingRunnable)
        job.cancel()
        Log.d("test", "cancel called")

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
    private fun getCategories() {
        if (categoryList.isEmpty().not()) {
            stopMainCategoryShimmer()
            initMainCategoryRv(categoryList)
            return
        }


        viewModel.getCategories()
        lifecycleScope.launchWhenResumed {
            viewModel.categoryList.collect { result ->
                when (result) {
                    is Resource.Success -> {
                        stopMainCategoryShimmer()

                        result.data?.let {
                            initMainCategoryRv(it)
                            categoryList.clear()
                            categoryList.addAll(it)
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

    @RequiresApi(Build.VERSION_CODES.M)
    private fun getProducts() {
        if (productsList.isEmpty().not()) {
            stopDealsShimmer()
            initOfferedRv(productsList)
            return
        }

        viewModel.getOfferedProducts()
        lifecycleScope.launchWhenResumed {
            viewModel.offeredProducts.collect { result ->
                when (result) {
                    is Resource.Success -> {
                        stopDealsShimmer()
                        result.data?.let {
                            productsList.clear()
                            productsList.addAll(it)
                            initOfferedRv(productsList)
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
                        startMainCategoryShimmer()
                    }

                    else -> {}
                }
            }

        }
    }

    private fun noInternetCallBack() {
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.noInternet.collect {
                    Toast.makeText(context, "No Internet connection", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun startBannerShimmer() {
        binding.shimmerBanner.visibility = View.VISIBLE
        binding.shimmerBanner.startShimmer()
    }

    private fun stopBannerShimmer() {
        binding.shimmerBanner.stopShimmer()
        binding.shimmerBanner.visibility = View.INVISIBLE
    }

    private fun startMainCategoryShimmer() {
        binding.shimmerMainCategory.visibility = View.VISIBLE
        binding.shimmerMainCategory.startShimmer()
        binding.categoriesArea.visibility = View.INVISIBLE
        binding.shimmerCategoriesArea.visibility = View.VISIBLE
        binding.shimmerCategoriesArea.startShimmer()
    }

    private fun stopMainCategoryShimmer() {
        binding.shimmerMainCategory.stopShimmer()
        binding.shimmerMainCategory.visibility = View.INVISIBLE
        binding.categoriesArea.visibility = View.VISIBLE
        binding.shimmerCategoriesArea.stopShimmer()
        binding.shimmerCategoriesArea.visibility = View.INVISIBLE
    }

    private fun startDealsShimmer() {
        binding.shimmerDealsArea.startShimmer()
        binding.shimmerDealsTxt.startShimmer()
        binding.shimmerDealsArea.visibility = View.VISIBLE
        binding.shimmerDealsTxt.visibility = View.VISIBLE
        binding.bestDealsContainer.visibility = View.INVISIBLE

    }

    private fun stopDealsShimmer() {
        binding.shimmerDealsArea.stopShimmer()
        binding.shimmerDealsTxt.stopShimmer()
        binding.shimmerDealsArea.visibility = View.INVISIBLE
        binding.shimmerDealsTxt.visibility = View.INVISIBLE
        binding.bestDealsContainer.visibility = View.VISIBLE

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
        val adapter = BestDealsAdapter(data, this)
        binding.bestDealsRV.adapter = adapter
        RecyclerAnimation.animateRecycler(binding.bestDealsRV)
        adapter.notifyDataSetChanged()
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

    override fun cartOnClick(productId: String , product: Product) {
        if(firebaseAuth.currentUser == null){
            AppDialog().showingRegisterDialogIfNotRegister(
                Constant.COULDNOT_ADD_TO_CART,
                Constant.PLS_LOGIN
            )
            return
        }
        addProductToCart(product)

    }
    private fun addProductToCart(product : Product) {
        viewModel.addProductToCart(
            firebaseAuth.currentUser?.email ?: "",
            product,
            -1 ,
            ""
        )
    }

    private fun addProductToCartCallback() {
        lifecycleScope.launchWhenResumed {
            viewModel.addToCartFlow.collect{
                when (it) {
                    is Resource.Success -> {
                        appDialog.dismissProgress()
                        Toast.makeText(context, "Product added successfully", Toast.LENGTH_SHORT).show()
                    }

                    is Resource.Loading -> {
                        appDialog.showProgressDialog()
                    }

                    is Resource.Failure -> {
                        appDialog.dismissProgress()
                        Toast.makeText(context, it.message, Toast.LENGTH_LONG).show()
                        Log.d("test" , "addProductToCartCallback "+it.message)
                    }

                    else -> {}
                }
            }
        }

    }

}

