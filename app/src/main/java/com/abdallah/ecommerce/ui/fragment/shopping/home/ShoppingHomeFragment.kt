package com.abdallah.ecommerce.ui.fragment.shopping.home

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.abdallah.ecommerce.R
import com.abdallah.ecommerce.data.model.Category
import com.abdallah.ecommerce.data.model.Product
import com.abdallah.ecommerce.databinding.FragmentShoppingHomeBinding
import com.abdallah.ecommerce.utils.Resource
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class ShoppingHomeFragment : Fragment(R.layout.fragment_shopping_home) {

    private lateinit var binding: FragmentShoppingHomeBinding
    private val viewModel by viewModels<ShoppingHomeViewModel>()

    private var bannerCurrentPosition = 0
    private var scrollingRunnable: Runnable = Runnable {}
    private val handler: Handler by lazy { Handler() }

    @Inject
    lateinit var firestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentShoppingHomeBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        downloadBannerImages()
        getCategories()
        getProducts()
        startBannerShimmer()
        startMainCategoryShimmer()

    }

    private fun downloadBannerImages() {
        viewModel.downloadAllImages()
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.imageList.collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            stopBannerShimmer()
                            result.data?.let {
                                createBanner(it)
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
    }


    private fun createBanner(uriList: ArrayList<Uri>) {
        var bannerAdapter = BannerRecAdapter(uriList)
        binding.bannerHomeParent.adapter = bannerAdapter
        bannerAdapter.notifyDataSetChanged()

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


private fun getCategories() {
    viewModel.getCategories()
    lifecycleScope.launch {
        viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.categoryList.collect { result ->
                when (result) {
                    is Resource.Success -> {
                        stopMainCategoryShimmer()
                        result.data?.let { initMainCategoryRv(it) }
                    }

                    is Resource.Failure -> {
                        stopMainCategoryShimmer()
//                            showLongToast("Error occurred " + result.message)
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
}
private fun getProducts() {
    viewModel.getOfferedProducts()
    lifecycleScope.launch {
        viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.offeredProducts.collect { result ->
                when (result) {
                    is Resource.Success -> {
                        stopDealsShimmer()
                        result.data?.let {initOfferedRv(it)}
                    }

                    is Resource.Failure -> {
                        stopDealsShimmer()
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
    binding.shimmerDealsArea.visibility = View.VISIBLE
    binding.shimmerMainCategory.startShimmer()

}

private fun stopDealsShimmer() {
    binding.shimmerDealsArea.stopShimmer()
    binding.shimmerDealsArea.visibility = View.INVISIBLE
}

private fun initMainCategoryRv(data: ArrayList<Category>) {
    val adapter = MainCategoryAdapter(data)
    binding.mainRecCategory.adapter = adapter
    adapter.notifyDataSetChanged()
}
    private fun initOfferedRv(data: ArrayList<Product>) {
    val adapter = BestDealsAdapter(data)
    binding.bestDealsRV.adapter = adapter
    adapter.notifyDataSetChanged()
}
}

