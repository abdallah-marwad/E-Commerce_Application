package com.abdallah.ecommerce.ui.fragment.shopping.home

import android.net.Uri
import android.os.Bundle
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
import androidx.lifecycle.whenStarted
import com.abdallah.ecommerce.R
import com.abdallah.ecommerce.databinding.FragmentShoppingHomeBinding
import com.abdallah.ecommerce.ui.fragment.loginRegister.registeration.RegisterViewModel
import com.abdallah.ecommerce.utils.Constant
import com.abdallah.ecommerce.utils.Resource
import com.google.firebase.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.storage
import com.yarolegovich.discretescrollview.DSVOrientation
import com.yarolegovich.discretescrollview.InfiniteScrollAdapter
import com.yarolegovich.discretescrollview.transform.ScaleTransformer
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class ShoppingHomeFragment : Fragment(R.layout.fragment_shopping_home) {

    private lateinit var binding: FragmentShoppingHomeBinding
    private val viewModel by viewModels<ShoppingHomeViewModel>()
    private var parentJob: Job = Job()
    private var coroutineScope: CoroutineScope = CoroutineScope(parentJob + Dispatchers.IO)
    private var bannerCurrentPosition = 0
    val imgList = ArrayList<Uri>()
    lateinit var bannerAdapter: InfiniteScrollAdapter<*>



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
        createBanner()

    }

    private fun downloadBannerImages() {
        startBannerShimmer()
        viewModel.downloadImage.downloadImage(Constant.HOME_BANNER_BATH + "1")
            .addOnSuccessListener {
                stopBannerShimmer()
                imgList.add(it)
                bannerAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                stopBannerShimmer()
            }
        viewModel.downloadImage.downloadImage(Constant.HOME_BANNER_BATH + "2")
            .addOnSuccessListener {
                stopBannerShimmer()
                imgList.add(it)
                bannerAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                stopBannerShimmer()
            }
        viewModel.downloadImage.downloadImage(Constant.HOME_BANNER_BATH + "3")
            .addOnSuccessListener {
                stopBannerShimmer()
                imgList.add(it)
                bannerAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                stopBannerShimmer()
            }

    }


    private fun startBannerShimmer(){
        binding.shimmerBanner.visibility = View.VISIBLE
        binding.shimmerBanner.startShimmer()
    }
    private fun stopBannerShimmer(){
        binding.shimmerBanner.visibility = View.INVISIBLE
        binding.shimmerBanner.stopShimmer()
    }
    private fun createBanner() {
        bannerAdapter = InfiniteScrollAdapter(BannerRecAdapter(imgList))
        binding.bannerHomeParent.adapter = bannerAdapter
        binding.bannerHomeParent.setOrientation(DSVOrientation.HORIZONTAL)
        binding.bannerHomeParent.setItemTransformer(
            ScaleTransformer.Builder()
                .setMinScale(0.8f)
                .build()
        )

    }


    override fun onPause() {
        super.onPause()
        parentJob.cancel()

    }

    override fun onResume() {
        super.onResume()
        autoLoopBanner()
    }

    private fun autoLoopBanner() {
        parentJob.cancel()
        parentJob = coroutineScope.launch() {
            while (true) {
                delay(3000L)
                val bannerLastItem = binding.bannerHomeParent.adapter?.itemCount ?: return@launch


                if (bannerCurrentPosition == bannerLastItem) {
                    bannerCurrentPosition = 0
                }
                withContext(Dispatchers.Main) {
                    binding.bannerHomeParent.smoothScrollToPosition(bannerCurrentPosition)
                }
                Log.d("test", "position is $bannerCurrentPosition")
                bannerCurrentPosition++
            }
        }

    }
//    private fun downloadBannerImages() {
//
//
//        viewModel.downloadAllImages()
//        lifecycleScope.launch {
//            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
//                viewModel.imageList.collect { result ->
//                    when (result) {
//                        is Resource.Success -> {
//                            stopBannerShimmer()
//                            result.data?.let {
//                                createBanner(it)
//                                autoLoopBanner()
//                            }
//                        }
//
//                        is Resource.Failure -> {
//                            stopBannerShimmer()
//                            Toast.makeText(
//                                context, "Error occured " + result.message,
//                                Toast.LENGTH_LONG
//                            ).show()
//                        }
//                        is Resource.Loading -> {
//                            startBannerShimmer()
//
//                        }
//                        else -> {}
//                    }
//
//
//                }
//            }
//
//        }
//    }
}