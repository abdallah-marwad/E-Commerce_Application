package com.abdallah.ecommerce.ui.fragment.shopping.home

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.abdallah.ecommerce.data.model.Category
import com.abdallah.ecommerce.databinding.FragmentShoppingHomeBinding
import com.abdallah.ecommerce.ui.fragment.shopping.home.mainCategory.MainCategoryAdapter
import com.abdallah.ecommerce.utils.Resource
import com.google.firebase.firestore.FirebaseFirestore
import com.abdallah.ecommerce.application.core.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class ShoppingHomeFragment : BaseFragment<FragmentShoppingHomeBinding>() {
    private val viewModel by viewModels<ShoppingHomeViewModel>()
    private var parentJob: Job = Job()
    private var coroutineScope: CoroutineScope = CoroutineScope(parentJob + Dispatchers.IO)
    private var bannerCurrentPosition = 0
    var stopLoop = false

    @Inject
    lateinit var firestore: FirebaseFirestore



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        downloadBannerImages()
        getCategories()

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
                            showLongToast("Error occurred " + result.message)

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
        stopLoop = true
    }

    override fun onResume() {
        super.onResume()
        stopLoop = false
        autoLoopBanner()
    }


    private fun autoLoopBanner() {
//        coroutineScope.launch() {
//            while (true) {
//
//                if (stopLoop)
//                    return@launch
//
//                Thread.sleep(5000L)
//
//                val bannerLastItem =
//                    binding.bannerHomeParent.adapter?.itemCount ?: return@launch
//                if (bannerCurrentPosition == bannerLastItem) {
//                    bannerCurrentPosition = 0
//                }
//                binding.bannerHomeParent.smoothScrollToPosition(bannerCurrentPosition)
//                Log.d("test", "position is $bannerCurrentPosition")
//                bannerCurrentPosition++
//            }
//        }
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
                            showLongToast("Error occurred " + result.message)
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
        binding.shimmerBanner.visibility = View.INVISIBLE
        binding.shimmerBanner.stopShimmer()
    }

    private fun startMainCategoryShimmer() {
        binding.shimmerMainCategory.visibility = View.VISIBLE
        binding.shimmerMainCategory.startShimmer()
    }
    private fun stopMainCategoryShimmer() {
        binding.shimmerMainCategory.visibility = View.INVISIBLE
        binding.shimmerMainCategory.stopShimmer()
    }
    private fun initMainCategoryRv(data: ArrayList<Category>) {
        val adapter = MainCategoryAdapter(data)
        binding.mainRecCategory.adapter = adapter
        adapter.notifyDataSetChanged()
    }
}

