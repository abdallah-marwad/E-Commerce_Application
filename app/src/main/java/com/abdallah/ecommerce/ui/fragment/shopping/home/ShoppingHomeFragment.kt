package com.abdallah.ecommerce.ui.fragment.shopping.home

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
import com.abdallah.ecommerce.databinding.FragmentShoppingHomeBinding
import com.abdallah.ecommerce.utils.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch


@AndroidEntryPoint
class ShoppingHomeFragment : Fragment(R.layout.fragment_shopping_home) {

    private lateinit var binding: FragmentShoppingHomeBinding
    private val viewModel by viewModels<ShoppingHomeViewModel>()
    private var parentJob: Job = Job()
    private var coroutineScope: CoroutineScope = CoroutineScope(parentJob + Dispatchers.IO)
    private var bannerCurrentPosition = 0
    var stopLoop = false


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

    private fun startBannerShimmer() {
        binding.shimmerBanner.visibility = View.VISIBLE
        binding.shimmerBanner.startShimmer()
    }

    private fun stopBannerShimmer() {
        binding.shimmerBanner.visibility = View.INVISIBLE
        binding.shimmerBanner.stopShimmer()
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
             coroutineScope.launch() {
                    while (true) {

                        if(stopLoop)
                            return@launch

                        Thread.sleep(5000L)

                        val bannerLastItem =
                            binding.bannerHomeParent.adapter?.itemCount ?: return@launch


                        if (bannerCurrentPosition == bannerLastItem) {
                            bannerCurrentPosition = 0
                        }

                            binding.bannerHomeParent.smoothScrollToPosition(bannerCurrentPosition)


                        Log.d("test", "position is $bannerCurrentPosition")
                        bannerCurrentPosition++
                    }
                }
            }
}

