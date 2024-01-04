package com.abdallah.ecommerce.ui.fragment.shopping.home

import android.annotation.SuppressLint
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
import androidx.navigation.fragment.findNavController
import com.abdallah.ecommerce.R
import com.abdallah.ecommerce.data.model.Categories
import com.abdallah.ecommerce.data.model.Category
import com.abdallah.ecommerce.data.model.Product
import com.abdallah.ecommerce.databinding.FragmentShoppingHomeBinding
import com.abdallah.ecommerce.ui.fragment.shopping.home.adapter.BannerRecAdapter
import com.abdallah.ecommerce.ui.fragment.shopping.home.adapter.BestDealsAdapter
import com.abdallah.ecommerce.ui.fragment.shopping.home.adapter.MainCategoryAdapter
import com.abdallah.ecommerce.utils.animation.RecyclerAnimation
import com.abdallah.ecommerce.utils.Resource
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class ShoppingHomeFragment : Fragment(R.layout.fragment_shopping_home) , MainCategoryAdapter.MainCategoryOnClick{

    private lateinit var binding: FragmentShoppingHomeBinding
    private val viewModel by viewModels<ShoppingHomeViewModel>()

    private var bannerCurrentPosition = 0
    private var scrollingRunnable: Runnable = Runnable {}
    private val handler: Handler by lazy { Handler() }
    private var categoryList: ArrayList<Category> = ArrayList()
    private var productsList: ArrayList<Product> = ArrayList()
    private var bannersList: ArrayList<Uri> = ArrayList()

    @Inject
    lateinit var firestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentShoppingHomeBinding.inflate(inflater)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startBannerShimmer()
        startMainCategoryShimmer()
        startDealsShimmer()
        downloadBannerImages()
        getCategories()
        getProducts()
        noInternetCallBack()

    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun downloadBannerImages() {
        if(bannersList.isEmpty().not()){
            stopBannerShimmer()
            createBanner(bannersList)
            autoLoopBanner()
            return
        }


        viewModel.downloadBannerImages()
        lifecycleScope.launchWhenResumed {
                viewModel.imageList.collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            stopBannerShimmer()
                            result.data?.let {
                                bannersList.addAll(it)
                                createBanner(bannersList)
                                autoLoopBanner()
                            }
                        }
                        is Resource.Failure -> {
                            stopBannerShimmer()
//                            showLongToast("Error occurred " + result.message)
                            Toast.makeText(
                                context, "Error occured " + result.message,
                                Toast.LENGTH_LONG
                            ).show()
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
    }

    override fun onResume() {
        super.onResume()
        autoLoopBanner()
    }


    @SuppressLint("SuspiciousIndentation")
    private fun autoLoopBanner() {
        handler.removeCallbacks(scrollingRunnable)
        scrollingRunnable = Runnable {
            handler.postDelayed(scrollingRunnable , 5000L)

            val bannerLastItem =
                binding.bannerHomeParent.adapter?.itemCount ?: return@Runnable
            if (bannerCurrentPosition == bannerLastItem) {
                bannerCurrentPosition = 0
            }
            binding.bannerHomeParent.smoothScrollToPosition(bannerCurrentPosition)

            bannerCurrentPosition++
        }
        handler.post(scrollingRunnable)
    }


@RequiresApi(Build.VERSION_CODES.M)
private fun getCategories() {
    if(categoryList.isEmpty().not()) {
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
                            categoryList.addAll(it)
                        }
                    }

                    is Resource.Failure -> {
                        stopMainCategoryShimmer()
                        Toast.makeText(
                            context, "Error occured " + result.message,
                            Toast.LENGTH_LONG
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
    if(productsList.isEmpty().not()){
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
                            productsList.addAll(it)
                            initOfferedRv(productsList)
                        }
                    }

                    is Resource.Failure -> {
                        stopDealsShimmer()
                        Toast.makeText(
                            context, "Error occured " + result.message,
                            Toast.LENGTH_LONG
                        ).show()
                    Log.d("test" , "Firebase Err ${result.message}")
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
                    Toast.makeText(context , "No Internet connection" , Toast.LENGTH_SHORT).show()
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
    val adapter = MainCategoryAdapter(data , this)
    binding.mainRecCategory.adapter = adapter
    RecyclerAnimation.animateRecycler( binding.mainRecCategory)
    adapter.notifyDataSetChanged()
    binding.bannerHomeParent.scheduleLayoutAnimation()
}
    @SuppressLint("SuspiciousIndentation")
    private fun initOfferedRv(data: ArrayList<Product>) {
    val adapter = BestDealsAdapter(data)
    binding.bestDealsRV.adapter = adapter
        RecyclerAnimation.animateRecycler(binding.bestDealsRV)
        adapter.notifyDataSetChanged()
        binding.bannerHomeParent.scheduleLayoutAnimation()
}
    private fun createBanner(uriList: ArrayList<Uri>) {
        var bannerAdapter = BannerRecAdapter(uriList)
        binding.bannerHomeParent.adapter = bannerAdapter
        RecyclerAnimation.animateRecycler( binding.bannerHomeParent)
        bannerAdapter.notifyDataSetChanged()
        binding.bannerHomeParent.scheduleLayoutAnimation()
    }

    override fun mainCategoryOnClick(position: Int , categoryName : String) {
        val categories  = Categories()
        categories.addAll(categoryList)
        val action = ShoppingHomeFragmentDirections.actionHomeFragmentToAllProducts(categories , categoryName , position)
        findNavController().navigate(action)
    }

}

